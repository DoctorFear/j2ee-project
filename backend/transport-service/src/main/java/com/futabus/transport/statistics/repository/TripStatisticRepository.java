package com.futabus.transport.statistics.repository;

import com.futabus.transport.statistics.entity.TripStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripStatisticRepository extends JpaRepository<TripStatistic, Long> {
}
