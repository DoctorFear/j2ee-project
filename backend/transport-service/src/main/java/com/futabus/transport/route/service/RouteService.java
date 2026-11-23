package com.futabus.transport.route.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.route.dto.request.RouteCreationRequest;
import com.futabus.transport.route.dto.request.RouteUpdateRequest;
import com.futabus.transport.route.dto.response.RouteResponse;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.enums.RouteStatus;
import com.futabus.transport.route.exception.RouteErrorCode;
import com.futabus.transport.route.mapper.RouteMapper;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.route.repository.RouteStopRepository;
import com.futabus.transport.trip.repository.TripRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RouteService {
    RouteRepository routeRepository;
    RouteStopRepository routeStopRepository;
    TripRepository tripRepository;
    RouteMapper routeMapper;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public RouteResponse create(RouteCreationRequest request) {
        validateVersion(
                request.getCode(),
                request.getVersionNo(),
                request.getEffectiveFromAt(),
                request.getEffectiveToAt(),
                null
        );
        Route route = routeMapper.toRoute(request);
        route.setStatus(RouteStatus.ACTIVE);
        if (route.getVersionNo() <= 0) {
            int version = request.getVersionNo() != null && request.getVersionNo() > 0
                    ? request.getVersionNo()
                    : 1;
            route.setVersionNo(version);
        }
        Route savedRoute = routeRepository.save(route);

        return routeMapper.toRouteResponse(savedRoute);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public RouteResponse update(Long id, RouteUpdateRequest request) {
        Route route = getLockedRoute(id);
        validateVersion(
                request.getCode(),
                request.getVersionNo(),
                request.getEffectiveFromAt(),
                request.getEffectiveToAt(),
                id
        );
        if (tripRepository.existsByRouteId(id) && changesPinnedData(route, request)) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_PINNED);
        }

        routeMapper.updateRoute(request, route);
        return routeMapper.toRouteResponse(route);
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> findAll() {
        return routeRepository.findAll().stream().map(routeMapper::toRouteResponse).toList();
    }

    @Transactional(readOnly = true)
    public RouteResponse findById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_NOT_FOUND));

        return routeMapper.toRouteResponse(route);
    }

    @Transactional
    public void delete(Long id) {
        Route route = getLockedRoute(id);
        if (tripRepository.existsByRouteId(id)) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_PINNED);
        }
        if (routeStopRepository.existsByRouteId(id)) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_HAS_STOPS);
        }

        routeRepository.delete(route);
    }

    private Route getLockedRoute(Long id) {
        return routeRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_NOT_FOUND));
    }

    private void validateVersion(
            String code,
            Integer versionNo,
            LocalDateTime start,
            LocalDateTime end,
            Long currentId
    ) {
        boolean invalidEffectivePeriod = end != null && !start.isBefore(end);
        if (invalidEffectivePeriod) {
            throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_INVALID_EFFECTIVE_PERIOD);
        }

        for (Route version : routeRepository.findByCode(code)) {
            if (version.getId().equals(currentId)) {
                continue;
            }
            if (version.getVersionNo() == versionNo) {
                throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_VERSION_EXISTS);
            }
            LocalDateTime existingEnd = version.getEffectiveToAt();
            boolean periodsOverlap = (end == null || version.getEffectiveFromAt().isBefore(end))
                    && (existingEnd == null || start.isBefore(existingEnd));
            if (periodsOverlap) {
                throw new BusinessException(RouteErrorCode.TRANSPORT_ROUTE_PERIOD_OVERLAP);
            }
        }
    }

    private boolean changesPinnedData(Route route, RouteUpdateRequest request) {
        LocalDateTime end = request.getEffectiveToAt();
        boolean routeDetailsChanged = !route.getCode().equals(request.getCode())
                || route.getVersionNo() != request.getVersionNo()
                || !route.getName().equals(request.getName())
                || route.getBasePrice().compareTo(request.getBasePrice()) != 0
                || !route.getEffectiveFromAt().equals(request.getEffectiveFromAt());
        boolean newEndDate = end != null && !Objects.equals(end, route.getEffectiveToAt());
        return routeDetailsChanged || (newEndDate
                && tripRepository.existsByRouteIdAndDepartureAtGreaterThanEqual(route.getId(), end));
    }
}
