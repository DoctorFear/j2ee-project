package com.futabus.transport.trip.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripCreationRequest {
    @NotBlank
    @Size(max = 255)
    String code;

    @NotNull
    @Positive
    Long routeId;

    @NotNull
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
