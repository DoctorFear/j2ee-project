package com.futabus.transport.route.repository;

import com.futabus.transport.route.entity.StationAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StationAddressRepository extends JpaRepository<StationAddress, Long> {
    Optional<StationAddress> findFirstByStationIdAndEffectiveFromAtLessThanEqualOrderByEffectiveFromAtDesc(
            Long stationId, LocalDateTime at);

    List<StationAddress> findByStationIdOrderByEffectiveFromAtDesc(Long stationId);

    @Query("""
            select a from StationAddress a where a.effectiveFromAt = (
                select max(b.effectiveFromAt) from StationAddress b
                where b.station.id = a.station.id and b.effectiveFromAt <= :at
            )
            """)
    List<StationAddress> findEffectiveAt(LocalDateTime at);
}
