package com.futabus.transport.route.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.futabus.transport.route.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findAllByOrderByRouteIdAscStopOrderAsc();

    List<RouteStop> findByRouteIdOrderByStopOrderAsc(Long routeId);

    boolean existsByRouteId(Long routeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from RouteStop e where e.id = :id")
    Optional<RouteStop> findLockedById(Long id);

    Optional<RouteStop> findByRouteIdAndStopOrder(Long routeId, int stopOrder);
}
