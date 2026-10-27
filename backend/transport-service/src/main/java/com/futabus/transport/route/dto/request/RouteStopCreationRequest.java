package com.futabus.transport.route.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteStopCreationRequest {
    @NotNull
    @Positive
    Long routeId;

    @NotNull
    @Positive
    Long stationId;

    @NotNull
    @PositiveOrZero
    Integer stopOrder;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    BigDecimal distanceKm;

    @NotNull
    @PositiveOrZero
    Integer offsetMinutes;

    @NotNull
    Boolean allowPickup;

    @NotNull
    Boolean allowDropOff;
}
