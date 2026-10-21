package com.futabus.transport.route.repository;

import com.futabus.transport.route.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findAllByOrderByRouteIdAscStopOrderAsc();

    List<RouteStop> findByRouteIdOrderByStopOrderAsc(Long routeId);

    boolean existsByRouteIdAndStopOrder(Long routeId, int stopOrder);

    boolean existsByRouteIdAndStopOrderAndIdNot(Long routeId, int stopOrder, Long id);

    boolean existsByRouteId(Long routeId);
}
