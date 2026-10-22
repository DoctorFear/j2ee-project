package com.futabus.transport.trip.entity;

import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.trip.enums.RoleDriver;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
        name = "trip_drivers",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"trip_id", "driver_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripDriver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RoleDriver role;
}
