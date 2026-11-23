package com.futabus.transport.resource.dto.request;

import com.futabus.transport.resource.enums.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public record VehicleStatusRequest(
        @NotNull VehicleStatus status
) {
}
