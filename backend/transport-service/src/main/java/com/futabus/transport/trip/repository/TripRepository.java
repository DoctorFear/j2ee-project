package com.futabus.transport.trip.repository;

import com.futabus.transport.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    Optional<Trip> findByCode(String code);

    boolean existsByCode(String code);
}
