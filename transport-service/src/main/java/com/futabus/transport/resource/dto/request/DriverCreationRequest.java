package com.futabus.transport.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DriverCreationRequest {
    @NotBlank
    @Size(max = 255)
    String fullName;

    @NotBlank
    @Size(max = 255)
    String phone;

    @Size(max = 255)
    String licenseNumber;

    @Size(max = 255)
    String licenseClass;

    LocalDateTime licenseExpiryOn;
}
