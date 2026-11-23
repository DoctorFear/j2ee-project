package com.futabus.transport.statistics.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripStatisticIngestRequest {
    @NotNull
    @Positive
    Long tripId;

    @NotNull
    @PositiveOrZero
    Integer validTicketCount;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 15, fraction = 0)
    BigDecimal collectedAmount;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 15, fraction = 0)
    BigDecimal refundedAmount;

    @NotNull
    @Positive
    @DecimalMax("18446744073709551615")
    BigInteger sourceRevision;

    @NotNull
    LocalDateTime sourceAsOfAt;
}
