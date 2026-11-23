package com.futabus.transport.trip.repository;

import com.futabus.transport.trip.entity.TripDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.futabus.transport.trip.enums.RoleDriver;

@Repository
public interface TripDriverRepository extends JpaRepository<TripDriver, Long> {
    boolean existsByTripIdAndDriverId(Long tripId, Long driverId);

    boolean existsByTripId(Long tripId);

    boolean existsByDriverId(Long driverId);

    boolean existsByTripIdAndRole(Long tripId, RoleDriver role);

    List<TripDriver> findByTripIdOrderByDriverIdAsc(Long tripId);

    List<TripDriver> findByDriverId(Long driverId);
}
