package com.futabus.transport.route.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.futabus.transport.route.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Station e where e.id = :id")
    Optional<Station> findLockedById(Long id);
}
