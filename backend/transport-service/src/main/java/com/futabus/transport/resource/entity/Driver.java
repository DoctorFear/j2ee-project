package com.futabus.transport.resource.entity;

import com.futabus.transport.resource.enums.DriverStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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

    @Column(nullable = false, length = 150)
    String fullName;

    @Column(nullable = false, length = 20)
    String phone;

    @Column(length = 50)
    String licenseNumber;

    @Column(length = 20)
    String licenseClass;

    LocalDate licenseExpiryOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DriverStatus status;
}
