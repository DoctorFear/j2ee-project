package com.futabus.transport.route.service;

import com.futabus.transport.route.dto.request.RouteCreationRequest;
import com.futabus.transport.route.dto.response.RouteResponse;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.enums.RouteStatus;
import com.futabus.transport.route.mapper.RouteMapper;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.route.repository.RouteStopRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RouteService {
    RouteRepository routeRepository;
    RouteStopRepository routeStopRepository;
    RouteMapper routeMapper;

    @Transactional
    public RouteResponse create(RouteCreationRequest request) {
        validateEffectiveDates(request.getEffectiveFromAt(), request.getEffectiveToAt());

        if (routeRepository.existsByCodeAndVersionNo(request.getCode(), request.getVersionNo())) {
            throw new ResponseStatusException(CONFLICT, "Route code and version already exist");
        }

        Route route = routeMapper.toRoute(request);
        route.setStatus(RouteStatus.INACTIVE);

        return routeMapper.toRouteResponse(routeRepository.save(route));
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> findAll() {
        return routeRepository.findAll()
                .stream()
                .map(routeMapper::toRouteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RouteResponse findById(Long id) {
        return routeMapper.toRouteResponse(getRoute(id));
    }

    @Transactional
    public void delete(Long id) {
        Route route = getRoute(id);

        if (routeStopRepository.existsByRouteId(id)) {
            throw new ResponseStatusException(CONFLICT, "Delete the route stops before deleting this route");
        }

        routeRepository.delete(route);
    }

    private Route getRoute(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Route not found: " + id));
    }

    private void validateEffectiveDates(LocalDateTime effectiveFromAt, LocalDateTime effectiveToAt) {
        if (effectiveToAt != null && !effectiveToAt.isAfter(effectiveFromAt)) {
            throw new ResponseStatusException(BAD_REQUEST, "effectiveToAt must be after effectiveFromAt");
        }
    }
}
