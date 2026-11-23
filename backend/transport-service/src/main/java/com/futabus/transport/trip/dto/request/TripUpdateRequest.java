package com.futabus.transport.trip.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripUpdateRequest {
    @NotBlank
    @Size(max = 60)
    String code;

    @NotNull
    @Positive
    Long routeId;

    @Positive
    Long vehicleId;

    @NotNull
    LocalDateTime departureAt;

    @NotNull
    LocalDateTime arrivalAt;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 15, fraction = 0)
    BigDecimal ticketPrice;
}
