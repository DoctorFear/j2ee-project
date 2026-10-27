package com.futabus.transport.resource.entity;

import com.futabus.transport.resource.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String fullName;

    @Column(nullable = false)
    String phone;

    String licenseNumber;

    String licenseClass;

    LocalDateTime licenseExpiryOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DriverStatus status;
}
