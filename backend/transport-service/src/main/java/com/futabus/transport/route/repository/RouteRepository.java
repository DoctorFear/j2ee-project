package com.futabus.transport.route.repository;

import com.futabus.transport.route.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByCodeAndVersionNo(String code, int versionNo);

    boolean existsByCodeAndVersionNoAndIdNot(String code, int versionNo, Long id);
}
