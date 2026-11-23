package com.futabus.transport.trip.entity;

import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.trip.enums.SaleStatus;
import com.futabus.transport.trip.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "trips",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_trip_code",
        columnNames = "code"
    ),
    indexes = {
        @Index(name = "idx_trips_route_departure", columnList = "route_id, departure_at"),
        @Index(name = "idx_trips_vehicle_departure", columnList = "vehicle_id, departure_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 60)
    String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    Vehicle vehicle;

    @Column(nullable = false)
    LocalDateTime departureAt;

    @Column(nullable = false)
    LocalDateTime arrivalAt;

    LocalDateTime publishedAt;

    LocalDateTime estimatedDepartureAt;

    LocalDateTime estimatedArrivalAt;

    LocalDateTime actualDepartureAt;

    LocalDateTime actualArrivalAt;

    LocalDateTime cancelledAt;

    @Column(length = 255)
    String cancelReason;

    @Column(nullable = false, precision = 15, scale = 0)
    BigDecimal ticketPrice;

    Integer capacitySnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TripStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SaleStatus saleStatus;
}
