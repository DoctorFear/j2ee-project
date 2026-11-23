package com.futabus.transport.trip.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.futabus.transport.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.futabus.transport.trip.enums.TripStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {
    Optional<Trip> findByCode(String code);

    boolean existsByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Trip e where e.id = :id")
    Optional<Trip> findLockedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Trip t where t.code = :code")
    Optional<Trip> findLockedByCode(String code);

    boolean existsByRouteId(Long routeId);

    boolean existsByVehicleId(Long vehicleId);

    boolean existsByRouteIdAndDepartureAtGreaterThanEqual(Long routeId, LocalDateTime departureAt);

    List<Trip> findByVehicleIdAndStatusIn(Long vehicleId, Collection<TripStatus> statuses);

    @Query("""
            select a.trip
            from TripDriver a
            where a.driver.id = :driverId and a.trip.status in :statuses
            """)
    List<Trip> findByDriverAndStatuses(Long driverId, Collection<TripStatus> statuses);
}
