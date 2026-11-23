package com.futabus.transport.route.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.futabus.transport.route.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByCodeAndVersionNoAndIdNot(String code, int versionNo, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Route e where e.id = :id")
    Optional<Route> findLockedById(Long id);

    List<Route> findByCode(String code);
}
