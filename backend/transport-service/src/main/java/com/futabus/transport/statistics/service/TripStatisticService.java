package com.futabus.transport.statistics.service;

import com.futabus.transport.exception.BusinessException;
import com.futabus.transport.statistics.dto.request.TripStatisticCreationRequest;
import com.futabus.transport.statistics.dto.response.TripStatisticResponse;
import com.futabus.transport.statistics.entity.TripStatistic;
import com.futabus.transport.statistics.exception.TripStatisticErrorCode;
import com.futabus.transport.statistics.mapper.TripStatisticMapper;
import com.futabus.transport.statistics.repository.TripStatisticRepository;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.exception.TripErrorCode;
import com.futabus.transport.trip.repository.TripRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripStatisticService {
    TripStatisticRepository tripStatisticRepository;
    TripRepository tripRepository;
    TripStatisticMapper tripStatisticMapper;

    @Transactional
    public TripStatisticResponse create(TripStatisticCreationRequest request) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new BusinessException(TripErrorCode.TRANSPORT_TRIP_NOT_FOUND));

        if (tripStatisticRepository.existsById(request.getTripId())) {
            throw new BusinessException(TripStatisticErrorCode.TRANSPORT_TRIP_STATISTIC_ALREADY_EXISTS);
        }

        TripStatistic tripStatistic = tripStatisticMapper.toTripStatistic(request);
        tripStatistic.setTrip(trip);

        return tripStatisticMapper.toTripStatisticResponse(tripStatisticRepository.save(tripStatistic));
    }

    @Transactional(readOnly = true)
    public List<TripStatisticResponse> findAll() {
        return tripStatisticRepository.findAll()
                .stream()
                .map(tripStatisticMapper::toTripStatisticResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripStatisticResponse findById(Long tripId) {
        return tripStatisticMapper.toTripStatisticResponse(getTripStatistic(tripId));
    }

    @Transactional
    public void delete(Long tripId) {
        TripStatistic tripStatistic = getTripStatistic(tripId);
        tripStatisticRepository.delete(tripStatistic);
    }

    private TripStatistic getTripStatistic(Long tripId) {
        return tripStatisticRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(TripStatisticErrorCode.TRANSPORT_TRIP_STATISTIC_NOT_FOUND));
    }
}
