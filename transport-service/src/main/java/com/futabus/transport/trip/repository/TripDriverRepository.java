package com.futabus.transport.trip.repository;

import com.futabus.transport.trip.entity.TripDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripDriverRepository extends JpaRepository<TripDriver, Long> {
    boolean existsByTripIdAndDriverId(Long tripId, Long driverId);

    boolean existsByTripId(Long tripId);

    void deleteByTripId(Long tripId);
}
