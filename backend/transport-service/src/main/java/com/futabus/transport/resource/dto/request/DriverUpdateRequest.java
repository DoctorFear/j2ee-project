package com.futabus.transport.resource.dto.request;

import com.futabus.transport.resource.enums.DriverStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DriverUpdateRequest {
    @NotBlank
    @Size(max = 150)
    String fullName;

    @NotBlank
    @Size(max = 20)
    String phone;

    @Size(max = 50)
    String licenseNumber;

    @Size(max = 20)
    String licenseClass;

    LocalDate licenseExpiryOn;

    @NotNull
    DriverStatus status;
}
