package com.futabus.transport.route.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record StationSegmentCreationRequest(
        @NotNull @Positive Long fromStationId,
        @NotNull @Positive Long toStationId,
        @NotNull @Positive @Digits(integer = 8, fraction = 2) BigDecimal distanceKm,
        @NotNull @Positive Integer durationMinutes
) {
}
