package com.futabus.transport.route.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.route.dto.request.RouteStopCreationRequest;
import com.futabus.transport.route.dto.request.RouteStopUpdateRequest;
import com.futabus.transport.route.dto.response.RouteStopResponse;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.entity.RouteStop;
import com.futabus.transport.route.entity.Station;
import com.futabus.transport.route.exception.RouteErrorCode;
import com.futabus.transport.route.mapper.RouteStopMapper;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.route.repository.RouteStopRepository;
import com.futabus.transport.route.repository.StationRepository;
import com.futabus.transport.trip.repository.TripRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RouteStopService {
    RouteStopRepository routeStopRepository;
    RouteRepository routeRepository;
    StationRepository stationRepository;
    RouteStopMapper routeStopMapper;
    TripRepository tripRepository;

    @Transactional
    public RouteStopResponse create(RouteStopCreationRequest request) {
        Route route = lockEditableRoute(request.getRouteId());
        Station station = getStation(request.getStationId());
        validateStop(
                route.getId(),
                null,
                request.getStopOrder(),
                request.getDistanceKm(),
                request.getOffsetMinutes()
        );
        RouteStop stop = routeStopMapper.toRouteStop(request);
        stop.setRoute(route);
        stop.setStation(station);
        RouteStop savedStop = routeStopRepository.save(stop);

        return routeStopMapper.toRouteStopResponse(savedStop);
    }

    @Transactional
    public RouteStopResponse update(Long id, RouteStopUpdateRequest request) {
        RouteStop stop = getLockedStop(id);
        Long currentRouteId = stop.getRoute().getId();
        Long requestedRouteId = request.getRouteId();
        // Lock both routes in ascending ID order when moving a stop.
        if (currentRouteId < requestedRouteId) {
            lockEditableRoute(currentRouteId);
        }
        Route route = lockEditableRoute(requestedRouteId);
        if (currentRouteId > requestedRouteId) {
            lockEditableRoute(currentRouteId);
        }
        Station station = getStation(request.getStationId());
        validateStop(
                route.getId(),
                id,
                request.getStopOrder(),
                request.getDistanceKm(),
                request.getOffsetMinutes()
        );
        routeStopMapper.updateRouteStop(request, stop);
        stop.setRoute(route);
        stop.setStation(station);
        RouteStop savedStop = routeStopRepository.save(stop);

        return routeStopMapper.toRouteStopResponse(savedStop);
    }

    private void validateStop(
            Long routeId,
            Long stopId,
            Integer stopOrder,
            BigDecimal distanceKm,
            Integer offsetMinutes
    ) {
        boolean invalidOrigin = stopOrder == 1 && (distanceKm.signum() != 0 || offsetMinutes != 0);
        if (invalidOrigin) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_STOP_INVALID_ORIGIN);
        }
        RouteStop existingStop = routeStopRepository.findByRouteIdAndStopOrder(routeId, stopOrder).orElse(null);
        boolean orderUsedByAnotherStop = existingStop != null && !existingStop.getId().equals(stopId);
        if (orderUsedByAnotherStop) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_STOP_ORDER_EXISTS);
        }
    }

    @Transactional(readOnly = true)
    public List<RouteStopResponse> findAll(Long routeId) {
        List<RouteStop> routeStops;
        if (routeId == null) {
            routeStops = routeStopRepository.findAllByOrderByRouteIdAscStopOrderAsc();
        } else {
            getRoute(routeId);
            routeStops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(routeId);
        }

        return routeStops.stream().map(routeStopMapper::toRouteStopResponse).toList();
    }

    @Transactional(readOnly = true)
    public RouteStopResponse findById(Long id) {
        RouteStop stop = getRouteStop(id);

        return routeStopMapper.toRouteStopResponse(stop);
    }

    @Transactional
    public void delete(Long id) {
        RouteStop stop = getLockedStop(id);

        lockEditableRoute(stop.getRoute().getId());
        routeStopRepository.delete(stop);
    }

    private RouteStop getRouteStop(Long id) {
        return routeStopRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_STOP_NOT_FOUND));
    }

    private Route getRoute(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_NOT_FOUND));
    }

    private RouteStop getLockedStop(Long id) {
        return routeStopRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_STOP_NOT_FOUND));
    }

    private Route lockEditableRoute(Long id) {
        Route route = routeRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_NOT_FOUND));

        if (tripRepository.existsByRouteId(id)) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_PINNED);
        }

        return route;
    }

    private Station getStation(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_STATION_NOT_FOUND));
    }
}
