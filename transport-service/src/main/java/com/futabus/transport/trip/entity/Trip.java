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
@Table(name = "trips")
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

    @Column(nullable = false)
    String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    Vehicle vehicle;

    @Column(nullable = false)
    LocalDateTime departureAt;

    @Column(nullable = false)
    LocalDateTime arrivalAt;

    LocalDateTime estimatedDepartureAt;

    LocalDateTime estimatedArrivalAt;

    LocalDateTime actualDepartureAt;

    LocalDateTime actualArrivalAt;

    @Column(nullable = false, precision = 15, scale = 0)
    BigDecimal ticketPrice;

    int capacitySnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TripStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    SaleStatus saleStatus;
}
