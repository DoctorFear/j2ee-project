package com.futabus.transport.route.service;

import com.futabus.transport.route.dto.request.RouteStopCreationRequest;
import com.futabus.transport.route.dto.response.RouteStopResponse;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.entity.RouteStop;
import com.futabus.transport.route.entity.Station;
import com.futabus.transport.route.mapper.RouteStopMapper;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.route.repository.RouteStopRepository;
import com.futabus.transport.route.repository.StationRepository;
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
public class RouteStopService {
    RouteStopRepository routeStopRepository;
    RouteRepository routeRepository;
    StationRepository stationRepository;
    RouteStopMapper routeStopMapper;

    @Transactional
    public RouteStopResponse create(RouteStopCreationRequest request) {
        Route route = getRoute(request.getRouteId());
        Station station = getStation(request.getStationId());

        if (routeStopRepository.existsByRouteIdAndStopOrder(request.getRouteId(), request.getStopOrder())) {
            throw new ResponseStatusException(CONFLICT, "Stop order already exists in this route");
        }

        RouteStop routeStop = routeStopMapper.toRouteStop(request);
        routeStop.setRoute(route);
        routeStop.setStation(station);

        return routeStopMapper.toRouteStopResponse(routeStopRepository.save(routeStop));
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

        return routeStops.stream()
                .map(routeStopMapper::toRouteStopResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RouteStopResponse findById(Long id) {
        return routeStopMapper.toRouteStopResponse(getRouteStop(id));
    }

    @Transactional
    public void delete(Long id) {
        routeStopRepository.delete(getRouteStop(id));
    }

    private RouteStop getRouteStop(Long id) {
        return routeStopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Route stop not found: " + id));
    }

    private Route getRoute(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Route not found: " + id));
    }

    private Station getStation(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Station not found: " + id));
    }
}
