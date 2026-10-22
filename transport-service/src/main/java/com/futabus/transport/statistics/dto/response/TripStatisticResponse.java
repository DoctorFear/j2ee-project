package com.futabus.transport.statistics.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripStatisticResponse {
    Long tripId;

    int validTicketCount;

    BigDecimal collectedAmount;

    BigDecimal refundedAmount;

    LocalDateTime updatedAt;
}
