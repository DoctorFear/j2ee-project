package com.futabus.transport.trip.service;

import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.repository.VehicleRepository;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.trip.dto.request.TripCreationRequest;
import com.futabus.transport.trip.dto.response.TripResponse;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.enums.SaleStatus;
import com.futabus.transport.trip.enums.TripStatus;
import com.futabus.transport.trip.mapper.TripMapper;
import com.futabus.transport.trip.repository.TripDriverRepository;
import com.futabus.transport.trip.repository.TripRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripService {
    TripRepository tripRepository;
    TripDriverRepository tripDriverRepository;
    RouteRepository routeRepository;
    VehicleRepository vehicleRepository;
    TripMapper tripMapper;

    @Transactional
    public TripResponse create(TripCreationRequest request) {
        if (!request.getArrivalAt().isAfter(request.getDepartureAt())) {
            throw new ResponseStatusException(BAD_REQUEST, "arrivalAt must be after departureAt");
        }

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Route not found: " + request.getRouteId()));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Vehicle not found: " + request.getVehicleId()));

        Trip trip = tripMapper.toTrip(request);
        trip.setRoute(route);
        trip.setVehicle(vehicle);
        trip.setCapacitySnapshot(vehicle.getCapacity());
        trip.setStatus(TripStatus.DRAFT);
        trip.setSaleStatus(SaleStatus.NOT_OPEN);

        return tripMapper.toTripResponse(tripRepository.save(trip));
    }

    @Transactional(readOnly = true)
    public List<TripResponse> findAll() {
        return tripRepository.findAll()
                .stream()
                .map(tripMapper::toTripResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse findById(Long id) {
        return tripMapper.toTripResponse(getTrip(id));
    }

    @Transactional
    public void delete(Long id) {
        Trip trip = getTrip(id);

        if (tripDriverRepository.existsByTripId(id)) {
            throw new ResponseStatusException(CONFLICT, "Delete the trip drivers before deleting this trip");
        }

        tripRepository.delete(trip);
    }

    private Trip getTrip(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Trip not found: " + id));
    }
}
