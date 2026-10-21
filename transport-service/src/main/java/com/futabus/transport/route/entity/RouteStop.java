package com.futabus.transport.route.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(
        name = "route_stops",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"route_id", "stop_order"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    Station station;

    @Column(name = "stop_order", nullable = false)
    int stopOrder;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal distanceKm;

    @Column(nullable = false)
    int offsetMinutes;

    @Column(nullable = false)
    boolean allowPickup;

    @Column(nullable = false)
    boolean allowDropOff;
}
