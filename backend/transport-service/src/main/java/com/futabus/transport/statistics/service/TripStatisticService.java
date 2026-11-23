package com.futabus.transport.statistics.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.statistics.dto.TripStatisticIngestResult;
import com.futabus.transport.statistics.dto.request.TripStatisticIngestRequest;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripStatisticService {
    TripStatisticRepository tripStatisticRepository;
    TripRepository tripRepository;
    TripStatisticMapper tripStatisticMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TripStatisticIngestResult ingest(TripStatisticIngestRequest request) {
        Trip trip = tripRepository.findLockedById(request.getTripId())
                .orElseThrow(() -> new BusinessException(TripErrorCode.TRANSPORT_TRIP_NOT_FOUND));

        TripStatistic statistic = tripStatisticRepository.findById(trip.getId()).orElse(null);
        boolean created = statistic == null;

        if (created) {
            statistic = tripStatisticMapper.toTripStatistic(request);
            statistic.setTrip(trip);
        } else if (request.getSourceRevision().compareTo(statistic.getSourceRevision()) <= 0) {
            TripStatisticResponse response = tripStatisticMapper.toTripStatisticResponse(statistic);

            return TripStatisticIngestResult.builder()
                    .statistic(response)
                    .created(false)
                    .build();
        } else {
            tripStatisticMapper.updateStatistic(request, statistic);
        }

        statistic = tripStatisticRepository.saveAndFlush(statistic);
        TripStatisticResponse response = tripStatisticMapper.toTripStatisticResponse(statistic);

        return TripStatisticIngestResult.builder()
                .statistic(response)
                .created(created)
                .build();
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
        TripStatistic statistic = getTripStatistic(tripId);

        return tripStatisticMapper.toTripStatisticResponse(statistic);
    }

    private TripStatistic getTripStatistic(Long tripId) {
        return tripStatisticRepository.findById(tripId)
                .orElseThrow(() -> new BusinessException(TripStatisticErrorCode.TRANSPORT_TRIP_STATISTIC_NOT_FOUND));
    }
}
