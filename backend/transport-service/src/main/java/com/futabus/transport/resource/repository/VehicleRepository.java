package com.futabus.transport.resource.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.enums.ServiceType;
import com.futabus.transport.resource.enums.VehicleStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Vehicle e where e.id = :id")
    Optional<Vehicle> findLockedById(Long id);

    List<Vehicle> findByStatus(VehicleStatus status);

    List<Vehicle> findByType(ServiceType type);

    List<Vehicle> findByStatusAndType(VehicleStatus status, ServiceType type);
}
