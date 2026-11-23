package com.futabus.transport.route.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteStopCreationRequest {
    @NotNull
    @Positive
    Long routeId;

    @NotNull
    @Positive
    Long stationId;

    @NotNull
    @Positive
    Integer stopOrder;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    BigDecimal distanceKm;

    @NotNull
    @PositiveOrZero
    Integer offsetMinutes;

    @NotNull
    Boolean allowPickup = false;

    @NotNull
    Boolean allowDropOff = false;
}
