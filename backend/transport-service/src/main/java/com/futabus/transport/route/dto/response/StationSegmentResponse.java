package com.futabus.transport.route.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record StationSegmentResponse(
        Long id,
        Long fromStationId,
        Long toStationId,
        BigDecimal distanceKm,
        int durationMinutes
) {
}
