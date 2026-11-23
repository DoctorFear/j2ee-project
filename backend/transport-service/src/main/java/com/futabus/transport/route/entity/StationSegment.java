package com.futabus.transport.route.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(
    name = "station_segments",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_station_segment_direction",
        columnNames = { "from_station_id", "to_station_id" }
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_station_id", nullable = false)
    Station fromStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_station_id", nullable = false)
    Station toStation;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal distanceKm;

    @Column(nullable = false)
    int durationMinutes;
}
