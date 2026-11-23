package com.futabus.transport.resource.dto.response;

import com.futabus.transport.resource.enums.DriverStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DriverResponse {
    Long id;

    String fullName;

    String phone;

    String licenseNumber;

    String licenseClass;

    LocalDate licenseExpiryOn;

    DriverStatus status;
}
