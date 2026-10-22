package com.futabus.transport.statistics.service;

import com.futabus.transport.statistics.dto.request.TripStatisticCreationRequest;
import com.futabus.transport.statistics.dto.response.TripStatisticResponse;
import com.futabus.transport.statistics.entity.TripStatistic;
import com.futabus.transport.statistics.mapper.TripStatisticMapper;
import com.futabus.transport.statistics.repository.TripStatisticRepository;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.repository.TripRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Trip not found: " + request.getTripId()));

        if (tripStatisticRepository.existsById(request.getTripId())) {
            throw new ResponseStatusException(CONFLICT, "Trip statistic already exists for trip: " + request.getTripId());
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
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Trip statistic not found: " + tripId));
    }
}
