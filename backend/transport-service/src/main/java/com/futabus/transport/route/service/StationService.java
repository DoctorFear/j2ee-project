package com.futabus.transport.route.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.route.dto.request.StationAddressRequest;
import com.futabus.transport.route.dto.request.StationCreationRequest;
import com.futabus.transport.route.dto.request.StationUpdateRequest;
import com.futabus.transport.route.dto.response.StationAddressResponse;
import com.futabus.transport.route.dto.response.StationResponse;
import com.futabus.transport.route.entity.Station;
import com.futabus.transport.route.entity.StationAddress;
import com.futabus.transport.route.enums.StationStatus;
import com.futabus.transport.route.exception.RouteErrorCode;
import com.futabus.transport.route.mapper.StationMapper;
import com.futabus.transport.route.repository.StationAddressRepository;
import com.futabus.transport.route.repository.StationRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StationService {
    StationRepository stationRepository;
    StationAddressRepository stationAddressRepository;
    StationMapper stationMapper;
    Clock transportClock;

    @Transactional
    public StationResponse create(StationCreationRequest request) {
        Station station = stationMapper.toStation(request);
        station.setStatus(StationStatus.ACTIVE);
        station = stationRepository.save(station);
        saveAddress(station, request.getAddress(), LocalDateTime.now(transportClock));
        return toStationResponse(station, request.getAddress());
    }

    @Transactional(readOnly = true)
    public List<StationResponse> findAll() {
        var addresses = stationAddressRepository.findEffectiveAt(LocalDateTime.now(transportClock))
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getStation().getId(),
                        StationAddress::getAddress));
        return stationRepository.findAll()
                .stream()
                .map(station -> toStationResponse(station, addresses.get(station.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public StationResponse findById(Long id, LocalDateTime at) {
        Station station = getStation(id);
        LocalDateTime effectiveAt = at == null ? LocalDateTime.now(transportClock) : at;
        return toStationResponse(station, addressAt(id, effectiveAt));
    }

    @Transactional
    public StationResponse update(Long id, StationUpdateRequest request) {
        Station station = getLockedStation(id);
        LocalDateTime now = LocalDateTime.now(transportClock);
        if (!request.getAddress().equals(addressAt(id, now))) {
            saveAddress(station, request.getAddress(), now);
        }
        stationMapper.updateStation(request, station);
        return toStationResponse(station, request.getAddress());
    }

    @Transactional
    public StationAddressResponse addAddress(Long stationId, StationAddressRequest request) {
        Station station = getLockedStation(stationId);
        StationAddress address = saveAddress(station, request.address(), request.effectiveFromAt());

        return toAddressResponse(address);
    }

    @Transactional(readOnly = true)
    public List<StationAddressResponse> findAddresses(Long stationId) {
        getStation(stationId);
        List<StationAddress> addresses = stationAddressRepository.findByStationIdOrderByEffectiveFromAtDesc(stationId);

        return addresses.stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        Station station = getLockedStation(id);
        stationRepository.delete(station);
    }

    private StationAddress saveAddress(Station station, String address, LocalDateTime effectiveFromAt) {
        return stationAddressRepository.save(
                StationAddress.builder()
                        .station(station)
                        .address(address)
                        .effectiveFromAt(effectiveFromAt)
                        .build());
    }

    private String addressAt(Long id, LocalDateTime at) {
        return stationAddressRepository
                .findFirstByStationIdAndEffectiveFromAtLessThanEqualOrderByEffectiveFromAtDesc(id, at)
                .map(StationAddress::getAddress)
                .orElse(null);
    }

    private StationResponse toStationResponse(Station station, String address) {
        StationResponse result = stationMapper.toStationResponse(station);
        result.setAddress(address);
        return result;
    }

    private StationAddressResponse toAddressResponse(StationAddress address) {
        return StationAddressResponse.builder()
                .id(address.getId())
                .address(address.getAddress())
                .effectiveFromAt(address.getEffectiveFromAt())
                .build();
    }

    private Station getStation(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_STATION_NOT_FOUND));
    }

    private Station getLockedStation(Long id) {
        return stationRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_STATION_NOT_FOUND));
    }
}
