package com.futabus.transport.route.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteStopResponse {
    Long id;

    Long routeId;

    Long stationId;

    int stopOrder;

    BigDecimal distanceKm;

    int offsetMinutes;

    boolean allowPickup;

    boolean allowDropOff;
}
