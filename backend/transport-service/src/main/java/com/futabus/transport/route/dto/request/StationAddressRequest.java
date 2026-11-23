package com.futabus.transport.route.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record StationAddressRequest(
        @NotBlank @Size(max = 255) String address,
        @NotNull LocalDateTime effectiveFromAt
) {
}
