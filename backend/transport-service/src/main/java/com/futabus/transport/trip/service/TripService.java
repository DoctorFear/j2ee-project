package com.futabus.transport.trip.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.exception.ResourceErrorCode;
import com.futabus.transport.resource.repository.VehicleRepository;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.exception.RouteErrorCode;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.trip.dto.request.TripCreationRequest;
import com.futabus.transport.trip.dto.response.TripResponse;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.enums.SaleStatus;
import com.futabus.transport.trip.enums.TripStatus;
import com.futabus.transport.trip.exception.TripErrorCode;
import com.futabus.transport.trip.mapper.TripMapper;
import com.futabus.transport.trip.repository.TripDriverRepository;
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
public class TripService {
    TripRepository tripRepository;
    TripDriverRepository tripDriverRepository;
    RouteRepository routeRepository;
    VehicleRepository vehicleRepository;
    TripMapper tripMapper;

    @Transactional
    public TripResponse create(TripCreationRequest request) {
        if (!request.getArrivalAt().isAfter(request.getDepartureAt())) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_INVALID_SCHEDULE);
        }

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_NOT_FOUND));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_VEHICLE_NOT_FOUND));

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
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_HAS_DRIVERS);
        }

        tripRepository.delete(trip);
    }

    private Trip getTrip(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new BusinessException(TripErrorCode.TRANSPORT_TRIP_NOT_FOUND));
    }
}
