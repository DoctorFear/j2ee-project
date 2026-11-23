package com.futabus.transport.trip.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.enums.DriverStatus;
import com.futabus.transport.resource.enums.VehicleStatus;
import com.futabus.transport.resource.exception.ResourceErrorCode;
import com.futabus.transport.resource.repository.DriverRepository;
import com.futabus.transport.resource.repository.VehicleRepository;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.entity.RouteStop;
import com.futabus.transport.route.enums.RouteStatus;
import com.futabus.transport.route.enums.StationStatus;
import com.futabus.transport.route.exception.RouteErrorCode;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.route.repository.RouteStopRepository;
import com.futabus.transport.trip.dto.request.TripCreationRequest;
import com.futabus.transport.trip.dto.request.TripUpdateRequest;
import com.futabus.transport.trip.dto.response.TripResponse;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.entity.TripDriver;
import com.futabus.transport.trip.enums.RoleDriver;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripService {
    static EnumSet<TripStatus> RESERVED_STATUSES = EnumSet.of(TripStatus.SCHEDULED, TripStatus.RUNNING);

    TripRepository tripRepository;
    TripDriverRepository tripDriverRepository;
    RouteRepository routeRepository;
    RouteStopRepository routeStopRepository;
    VehicleRepository vehicleRepository;
    DriverRepository driverRepository;
    TripMapper tripMapper;
    Clock transportClock;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TripResponse create(TripCreationRequest request) {
        Trip trip = new Trip();
        trip.setStatus(TripStatus.DRAFT);
        trip.setSaleStatus(SaleStatus.NOT_OPEN);
        applyDraftSchedule(
                trip,
                request.getCode(),
                request.getRouteId(),
                request.getVehicleId(),
                request.getDepartureAt(),
                request.getArrivalAt(),
                request.getTicketPrice());

        Trip savedTrip = tripRepository.save(trip);

        return tripMapper.toTripResponse(savedTrip);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TripResponse updateDraft(Long id, TripUpdateRequest request) {
        Trip trip = getLockedTrip(id);
        requireStatus(trip, TripStatus.DRAFT);
        applyDraftSchedule(
                trip,
                request.getCode(),
                request.getRouteId(),
                request.getVehicleId(),
                request.getDepartureAt(),
                request.getArrivalAt(),
                request.getTicketPrice());
        validateDraftCrew(trip);

        return tripMapper.toTripResponse(trip);
    }

    private void applyDraftSchedule(
            Trip trip,
            String code,
            Long routeId,
            Long vehicleId,
            LocalDateTime departureAt,
            LocalDateTime arrivalAt,
            BigDecimal ticketPrice) {
        boolean invalidSchedule = !departureAt.isBefore(arrivalAt);
        if (invalidSchedule) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_INVALID_SCHEDULE);
        }
        Route route = routeRepository.findLockedById(routeId)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_NOT_FOUND));
        Vehicle vehicle = null;
        if (vehicleId != null) {
            vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_VEHICLE_NOT_FOUND));
        }
        validateRoute(route, departureAt, arrivalAt);

        boolean needsUniqueCode = trip.getId() == null || !trip.getCode().equals(code);
        if (needsUniqueCode && tripRepository.existsByCode(code)) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_CODE_EXISTS);
        }

        trip.setCode(code);
        trip.setRoute(route);
        trip.setDepartureAt(departureAt);
        trip.setArrivalAt(arrivalAt);
        trip.setTicketPrice(ticketPrice);
        trip.setVehicle(vehicle);
        trip.setCapacitySnapshot(vehicle == null ? null : vehicle.getCapacity());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TripResponse publish(Long id) {
        Trip trip = getLockedTrip(id);
        requireStatus(trip, TripStatus.DRAFT);

        Route route = routeRepository.findLockedById(trip.getRoute().getId())
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_NOT_FOUND));
        validateRoute(route, trip.getDepartureAt(), trip.getArrivalAt());
        validateDispatch(trip, false);

        trip.setPublishedAt(LocalDateTime.now(transportClock));
        trip.setStatus(TripStatus.SCHEDULED);

        return tripMapper.toTripResponse(trip);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TripResponse assignVehicle(Long id, Long vehicleId) {
        Trip trip = getLockedTrip(id);
        boolean isDraft = trip.getStatus() == TripStatus.DRAFT;
        boolean scheduledBeforeSales = trip.getStatus() == TripStatus.SCHEDULED
                && trip.getSaleStatus() == SaleStatus.NOT_OPEN;

        if (!isDraft && !scheduledBeforeSales) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_VEHICLE_CHANGE_BLOCKED);
        }

        Vehicle vehicle = lockVehicle(vehicleId);
        trip.setVehicle(vehicle);
        trip.setCapacitySnapshot(vehicle.getCapacity());

        if (trip.getStatus() == TripStatus.SCHEDULED) {
            validateDispatch(trip, false);
        } else {
            validateDraftCrew(trip);
        }

        return tripMapper.toTripResponse(trip);
    }

    @Transactional
    public TripResponse changeSaleStatus(Long id, SaleStatus target) {
        Trip trip = getLockedTrip(id);
        requireStatus(trip, TripStatus.SCHEDULED);

        SaleStatus current = trip.getSaleStatus();
        if (target == current) {
            return tripMapper.toTripResponse(trip);
        }

        boolean validSaleTransition = switch (current) {
            case NOT_OPEN -> target == SaleStatus.OPEN || target == SaleStatus.CLOSED;
            case OPEN -> target == SaleStatus.PAUSED || target == SaleStatus.CLOSED;
            case PAUSED -> target == SaleStatus.OPEN || target == SaleStatus.CLOSED;
            case CLOSED -> false;
        };

        if (!validSaleTransition) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_SALE_TRANSITION_INVALID);
        }
        trip.setSaleStatus(target);

        return tripMapper.toTripResponse(trip);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TripResponse changeEstimate(Long id, LocalDateTime departure, LocalDateTime arrival) {
        Trip trip = getLockedTrip(id);
        boolean canChangeEstimate = trip.getStatus() == TripStatus.SCHEDULED || trip.getStatus() == TripStatus.RUNNING;

        if (!canChangeEstimate) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_TRANSITION_INVALID);
        }
        trip.setEstimatedDepartureAt(departure);
        trip.setEstimatedArrivalAt(arrival);

        LocalDateTime departureTime = resolveDepartureTime(trip);
        LocalDateTime arrivalTime = resolveArrivalTime(trip);
        boolean invalidEstimateWindow = !arrivalTime.isAfter(departureTime);
        boolean invalidEstimateOrder = departure != null && arrival != null && !departure.isBefore(arrival);

        if (invalidEstimateWindow || invalidEstimateOrder) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_INVALID_ESTIMATE);
        }
        validateDispatch(trip, false);

        return tripMapper.toTripResponse(trip);
    }

    @Transactional
    public TripResponse cancel(Long id, String reason) {
        Trip trip = getLockedTrip(id);
        boolean canCancel = trip.getStatus() == TripStatus.DRAFT || trip.getStatus() == TripStatus.SCHEDULED;

        if (!canCancel) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_TRANSITION_INVALID);
        }

        trip.setStatus(TripStatus.CANCELLED);
        trip.setSaleStatus(SaleStatus.CLOSED);
        trip.setCancelledAt(LocalDateTime.now(transportClock));
        trip.setCancelReason(reason.strip());

        return tripMapper.toTripResponse(trip);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Trip updateDrivingStatus(String code, TripStatus target) {
        Trip trip = tripRepository.findLockedByCode(code)
                .orElseThrow(() -> new BusinessException(TripErrorCode.TRANSPORT_TRIP_CODE_NOT_FOUND));

        if (target != TripStatus.RUNNING && target != TripStatus.COMPLETED) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_INVALID_STATUS);
        }
        if (target == trip.getStatus()) {
            return trip;
        }

        LocalDateTime now = LocalDateTime.now(transportClock);
        if (target == TripStatus.RUNNING) {
            requireStatus(trip, TripStatus.SCHEDULED);
            trip.setActualDepartureAt(now);
            if (!resolveArrivalTime(trip).isAfter(now)) {
                throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_INVALID_ESTIMATE);
            }
            trip.setStatus(target);
            trip.setSaleStatus(SaleStatus.CLOSED);
            validateDispatch(trip, true);

            return trip;
        }

        requireStatus(trip, TripStatus.RUNNING);
        if (!now.isAfter(trip.getActualDepartureAt())) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_TRANSITION_INVALID);
        }
        trip.setActualArrivalAt(now);
        trip.setStatus(target);
        trip.setSaleStatus(SaleStatus.CLOSED);

        return trip;
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
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new BusinessException(TripErrorCode.TRANSPORT_TRIP_NOT_FOUND));

        return tripMapper.toTripResponse(trip);
    }

    @Transactional
    public void delete(Long id) {
        Trip trip = getLockedTrip(id);
        requireStatus(trip, TripStatus.DRAFT);

        if (tripDriverRepository.existsByTripId(id)) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_HAS_DRIVERS);
        }

        tripRepository.delete(trip);
    }

    private void validateDraftCrew(Trip trip) {
        for (var assignment : tripDriverRepository.findByTripIdOrderByDriverIdAsc(trip.getId())) {
            validateDriver(
                    trip,
                    lockDriver(assignment.getDriver().getId()),
                    false);
        }
    }

    private Vehicle lockVehicle(Long id) {
        Vehicle vehicle = vehicleRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_VEHICLE_NOT_FOUND));
        if (vehicle.getStatus() != VehicleStatus.ACTIVE) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_VEHICLE_REQUIRED);
        }
        return vehicle;
    }

    private Driver lockDriver(Long id) {
        return driverRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_DRIVER_NOT_FOUND));
    }

    private void validateDriver(Trip trip, Driver driver, boolean departing) {
        if (driver.getStatus() != DriverStatus.ACTIVE) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_DRIVER_INELIGIBLE);
        }
        checkConflicts(trip, tripRepository.findByDriverAndStatuses(driver.getId(), RESERVED_STATUSES), departing);
    }

    private void validateDispatch(Trip trip, boolean departing) {
        if (trip.getVehicle() == null) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_VEHICLE_REQUIRED);
        }
        Vehicle vehicle = lockVehicle(trip.getVehicle().getId());
        List<TripDriver> crew = tripDriverRepository.findByTripIdOrderByDriverIdAsc(trip.getId());
        long primaryDriverCount = crew.stream()
                .filter(assignment -> assignment.getRole() == RoleDriver.PRIMARY)
                .count();
        if (primaryDriverCount != 1) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_PRIMARY_REQUIRED);
        }

        checkConflicts(
                trip,
                tripRepository.findByVehicleIdAndStatusIn(vehicle.getId(), RESERVED_STATUSES),
                departing);
        for (TripDriver assignment : crew) {
            validateDriver(trip, lockDriver(assignment.getDriver().getId()), departing);
        }
    }

    private void checkConflicts(Trip trip, List<Trip> candidates, boolean departing) {
        LocalDateTime departure = resolveDepartureTime(trip);
        LocalDateTime arrival = resolveArrivalTime(trip);

        for (Trip other : candidates) {
            if (other.getId().equals(trip.getId())) {
                continue;
            }
            boolean resourceStillInUse = departing && other.getStatus() == TripStatus.RUNNING;
            LocalDateTime otherDeparture = resolveDepartureTime(other);
            LocalDateTime otherArrival = resolveArrivalTime(other);
            boolean hasTimeConflict = departure.isBefore(otherArrival) && otherDeparture.isBefore(arrival);

            if (resourceStillInUse || hasTimeConflict) {
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

    private void requireStatus(Trip trip, TripStatus status) {
        if (trip.getStatus() != status) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_TRANSITION_INVALID);
        }
    }

    private void validateRoute(Route route, LocalDateTime departure, LocalDateTime arrival) {
        LocalDateTime effectiveTo = route.getEffectiveToAt();
        boolean routeEffectiveAtDeparture = route.getStatus() == RouteStatus.ACTIVE
                && !departure.isBefore(route.getEffectiveFromAt())
                && (effectiveTo == null || departure.isBefore(effectiveTo));
        if (!routeEffectiveAtDeparture) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_NOT_EFFECTIVE);
        }

        List<RouteStop> stops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(route.getId());
        if (stops.size() < 2) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_INVALID_ITINERARY);
        }
        RouteStop firstStop = stops.getFirst();
        RouteStop lastStop = stops.getLast();
        boolean firstStopOnlyPicksUp = firstStop.isAllowPickup() && !firstStop.isAllowDropOff();
        boolean lastStopOnlyDropsOff = lastStop.isAllowDropOff() && !lastStop.isAllowPickup();
        boolean validEndpoints = firstStopOnlyPicksUp && lastStopOnlyDropsOff;
        if (!validEndpoints || !departure.plusMinutes(lastStop.getOffsetMinutes()).equals(arrival)) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_INVALID_ITINERARY);
        }

        for (int i = 0; i < stops.size(); i++) {
            RouteStop stop = stops.get(i);
            boolean activeStation = stop.getStation().getStatus() == StationStatus.ACTIVE;
            boolean servesPassengers = stop.isAllowPickup() || stop.isAllowDropOff();
            if (stop.getStopOrder() != i + 1 || (!activeStation && servesPassengers)) {
                throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_INVALID_ITINERARY);
            }
            boolean invalidOrigin = i == 0 && (stop.getDistanceKm().signum() != 0 || stop.getOffsetMinutes() != 0);
            if (invalidOrigin) {
                throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_INVALID_ITINERARY);
            }
            if (i > 0) {
                RouteStop previous = stops.get(i - 1);
                boolean distanceIncreases = stop.getDistanceKm().compareTo(previous.getDistanceKm()) > 0;
                boolean offsetIncreases = stop.getOffsetMinutes() > previous.getOffsetMinutes();
                if (!distanceIncreases || !offsetIncreases) {
                    throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_INVALID_ITINERARY);
                }
            }
        }
    }
}
