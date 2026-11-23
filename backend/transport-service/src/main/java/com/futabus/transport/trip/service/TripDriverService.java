package com.futabus.transport.trip.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.resource.enums.DriverStatus;
import com.futabus.transport.resource.exception.ResourceErrorCode;
import com.futabus.transport.resource.repository.DriverRepository;
import com.futabus.transport.trip.dto.request.TripDriverCreationRequest;
import com.futabus.transport.trip.dto.response.TripDriverResponse;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.entity.TripDriver;
import com.futabus.transport.trip.enums.RoleDriver;
import com.futabus.transport.trip.enums.TripStatus;
import com.futabus.transport.trip.exception.TripErrorCode;
import com.futabus.transport.trip.mapper.TripDriverMapper;
import com.futabus.transport.trip.repository.TripDriverRepository;
import com.futabus.transport.trip.repository.TripRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripDriverService {
    static EnumSet<TripStatus> RESERVED_STATUSES = EnumSet.of(TripStatus.SCHEDULED, TripStatus.RUNNING);

    TripDriverRepository tripDriverRepository;
    TripRepository tripRepository;
    DriverRepository driverRepository;
    TripDriverMapper tripDriverMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TripDriverResponse create(TripDriverCreationRequest request) {
        Trip trip = getLockedTrip(request.getTripId());
        requireEditableCrew(trip);
        Driver driver = lockDriver(request.getDriverId());

        if (tripDriverRepository.existsByTripIdAndDriverId(request.getTripId(), request.getDriverId())) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_DRIVER_ALREADY_ASSIGNED);
        }

        boolean addingPrimaryDriver = request.getRole() == RoleDriver.PRIMARY;
        if (addingPrimaryDriver && tripDriverRepository.existsByTripIdAndRole(trip.getId(), RoleDriver.PRIMARY)) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_PRIMARY_EXISTS);
        }
        validateDriver(trip, driver);
        TripDriver tripDriver = tripDriverMapper.toTripDriver(request);
        tripDriver.setTrip(trip);
        tripDriver.setDriver(driver);
        TripDriver savedDriver = tripDriverRepository.save(tripDriver);

        return tripDriverMapper.toTripDriverResponse(savedDriver);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TripDriverResponse replacePrimary(Long tripId, Long driverId) {
        Trip trip = getLockedTrip(tripId);
        requireEditableCrew(trip);

        Driver driver = lockDriver(driverId);
        validateDriver(trip, driver);

        List<TripDriver> crew = tripDriverRepository.findByTripIdOrderByDriverIdAsc(tripId);
        TripDriver current = crew.stream()
                .filter(a -> a.getRole() == RoleDriver.PRIMARY)
                .findFirst()
                .orElse(null);
        if (current != null && current.getDriver().getId().equals(driverId)) {
            return tripDriverMapper.toTripDriverResponse(current);
        }
        if (current != null) {
            tripDriverRepository.delete(current);
            tripDriverRepository.flush();
        }
        TripDriver replacement = crew.stream()
                .filter(a -> a.getDriver().getId().equals(driverId))
                .findFirst()
                .orElseGet(() -> TripDriver.builder()
                        .trip(trip)
                        .driver(driver)
                        .build());
        replacement.setRole(RoleDriver.PRIMARY);
        TripDriver savedReplacement = tripDriverRepository.save(replacement);

        return tripDriverMapper.toTripDriverResponse(savedReplacement);
    }

    @Transactional(readOnly = true)
    public List<TripDriverResponse> findAll() {
        return tripDriverRepository.findAll().stream().map(tripDriverMapper::toTripDriverResponse).toList();
    }

    @Transactional(readOnly = true)
    public TripDriverResponse findById(Long id) {
        TripDriver tripDriver = getTripDriver(id);

        return tripDriverMapper.toTripDriverResponse(tripDriver);
    }

    @Transactional
    public void delete(Long id) {
        TripDriver tripDriver = getTripDriver(id);
        Trip trip = getLockedTrip(tripDriver.getTrip().getId());

        requireEditableCrew(trip);
        if (trip.getStatus() == TripStatus.SCHEDULED && tripDriver.getRole() == RoleDriver.PRIMARY) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_PRIMARY_REQUIRED);
        }
        tripDriverRepository.delete(tripDriver);
    }

    private Driver lockDriver(Long id) {
        return driverRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_DRIVER_NOT_FOUND));
    }

    private void validateDriver(Trip trip, Driver driver) {
        if (driver.getStatus() != DriverStatus.ACTIVE) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_DRIVER_INELIGIBLE);
        }
        List<Trip> candidates = tripRepository.findByDriverAndStatuses(driver.getId(), RESERVED_STATUSES);
        LocalDateTime departure = resolveDepartureTime(trip);
        LocalDateTime arrival = resolveArrivalTime(trip);

        for (Trip other : candidates) {
            if (other.getId().equals(trip.getId())) {
                continue;
            }
            LocalDateTime otherDeparture = resolveDepartureTime(other);
            LocalDateTime otherArrival = resolveArrivalTime(other);
            boolean hasTimeConflict = departure.isBefore(otherArrival) && otherDeparture.isBefore(arrival);

            if (hasTimeConflict) {
                throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_DISPATCH_CONFLICT);
            }
        }
    }

    private LocalDateTime resolveDepartureTime(Trip trip) {
        if (trip.getActualDepartureAt() != null) {
            return trip.getActualDepartureAt();
        }
        if (trip.getEstimatedDepartureAt() != null) {
            return trip.getEstimatedDepartureAt();
        }
        return trip.getDepartureAt();
    }

    private LocalDateTime resolveArrivalTime(Trip trip) {
        if (trip.getEstimatedArrivalAt() != null) {
            return trip.getEstimatedArrivalAt();
        }

        return trip.getArrivalAt();
    }

    private Trip getLockedTrip(Long id) {
        return tripRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(TripErrorCode.TRANSPORT_TRIP_NOT_FOUND));
    }

    private void requireEditableCrew(Trip trip) {
        boolean crewEditable = trip.getStatus() == TripStatus.DRAFT || trip.getStatus() == TripStatus.SCHEDULED;
        if (!crewEditable) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_TRANSITION_INVALID);
        }
    }

    private TripDriver getTripDriver(Long id) {
        return tripDriverRepository.findById(id)
                .orElseThrow(() -> new BusinessException(TripErrorCode.TRANSPORT_TRIP_DRIVER_NOT_FOUND));
    }

}
