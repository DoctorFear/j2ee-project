package com.futabus.transport.route.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record StationAddressResponse(
        Long id,
        String address,
        LocalDateTime effectiveFromAt
) {
}
