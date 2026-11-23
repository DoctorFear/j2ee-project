package com.futabus.transport.resource.entity;

import com.futabus.transport.resource.enums.ServiceType;
import com.futabus.transport.resource.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "vehicles",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_vehicle_license_plate",
        columnNames = "license_plate"
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 20)
    String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    ServiceType type;

    @Column(nullable = false)
    int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VehicleStatus status;
}
