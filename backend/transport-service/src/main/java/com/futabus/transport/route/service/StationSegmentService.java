package com.futabus.transport.route.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.route.dto.request.StationSegmentCreationRequest;
import com.futabus.transport.route.dto.request.StationSegmentUpdateRequest;
import com.futabus.transport.route.dto.response.StationSegmentResponse;
import com.futabus.transport.route.entity.Station;
import com.futabus.transport.route.entity.StationSegment;
import com.futabus.transport.route.exception.RouteErrorCode;
import com.futabus.transport.route.repository.StationRepository;
import com.futabus.transport.route.repository.StationSegmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StationSegmentService {
    StationSegmentRepository stationSegmentRepository;
    StationRepository stationRepository;

    @Transactional
    public StationSegmentResponse create(StationSegmentCreationRequest request) {
        if (request.fromStationId().equals(request.toStationId())) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_SEGMENT_SAME_STATION);
        }

        StationSegment segment = new StationSegment();
        segment.setFromStation(getStation(request.fromStationId()));
        segment.setToStation(getStation(request.toStationId()));
        segment.setDistanceKm(request.distanceKm());
        segment.setDurationMinutes(request.durationMinutes());
        StationSegment savedSegment = stationSegmentRepository.save(segment);

        return toStationSegmentResponse(savedSegment);
    }

    @Transactional
    public StationSegmentResponse update(Long id, StationSegmentUpdateRequest request) {
        if (request.fromStationId().equals(request.toStationId())) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_SEGMENT_SAME_STATION);
        }

        StationSegment segment = getSegment(id);
        segment.setFromStation(getStation(request.fromStationId()));
        segment.setToStation(getStation(request.toStationId()));
        segment.setDistanceKm(request.distanceKm());
        segment.setDurationMinutes(request.durationMinutes());
        StationSegment savedSegment = stationSegmentRepository.save(segment);

        return toStationSegmentResponse(savedSegment);
    }

    @Transactional(readOnly = true)
    public List<StationSegmentResponse> findAll() {
        return stationSegmentRepository.findAll()
                .stream()
                .map(this::toStationSegmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StationSegmentResponse findById(Long id) {
        StationSegment segment = getSegment(id);

        return toStationSegmentResponse(segment);
    }

    @Transactional
    public void delete(Long id) {
        StationSegment segment = getSegment(id);
        stationSegmentRepository.delete(segment);
    }

    private StationSegment getSegment(Long id) {
        return stationSegmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_SEGMENT_NOT_FOUND));
    }

    private Station getStation(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_STATION_NOT_FOUND));
    }

    private StationSegmentResponse toStationSegmentResponse(StationSegment segment) {
        return StationSegmentResponse.builder()
                .id(segment.getId())
                .fromStationId(segment.getFromStation().getId())
                .toStationId(segment.getToStation().getId())
                .distanceKm(segment.getDistanceKm())
                .durationMinutes(segment.getDurationMinutes())
                .build();
    }
}
