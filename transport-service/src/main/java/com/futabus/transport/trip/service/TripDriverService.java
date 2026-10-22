package com.futabus.transport.trip.service;

import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.resource.repository.DriverRepository;
import com.futabus.transport.trip.dto.request.TripDriverCreationRequest;
import com.futabus.transport.trip.dto.response.TripDriverResponse;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.entity.TripDriver;
import com.futabus.transport.trip.mapper.TripDriverMapper;
import com.futabus.transport.trip.repository.TripDriverRepository;
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
public class TripDriverService {
    TripDriverRepository tripDriverRepository;
    TripRepository tripRepository;
    DriverRepository driverRepository;
    TripDriverMapper tripDriverMapper;

    @Transactional
    public TripDriverResponse create(TripDriverCreationRequest request) {
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Trip not found: " + request.getTripId()));
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Driver not found: " + request.getDriverId()));

        if (tripDriverRepository.existsByTripIdAndDriverId(request.getTripId(), request.getDriverId())) {
            throw new ResponseStatusException(CONFLICT, "Driver already assigned to this trip");
        }

        TripDriver tripDriver = tripDriverMapper.toTripDriver(request);
        tripDriver.setTrip(trip);
        tripDriver.setDriver(driver);

        return tripDriverMapper.toTripDriverResponse(tripDriverRepository.save(tripDriver));
    }

    @Transactional(readOnly = true)
    public List<TripDriverResponse> findAll() {
        return tripDriverRepository.findAll()
                .stream()
                .map(tripDriverMapper::toTripDriverResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripDriverResponse findById(Long id) {
        return tripDriverMapper.toTripDriverResponse(getTripDriver(id));
    }

    @Transactional
    public void delete(Long id) {
        TripDriver tripDriver = getTripDriver(id);
        tripDriverRepository.delete(tripDriver);
    }

    private TripDriver getTripDriver(Long id) {
        return tripDriverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Trip driver not found: " + id));
    }
}
