package com.futabus.transport.route.dto.request;

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
public class RouteCreationRequest {
    @NotBlank
    @Size(max = 255)
    String code;

    @NotNull
    @Positive
    Integer versionNo;

    @NotBlank
    @Size(max = 255)
    String name;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 15, fraction = 0)
    BigDecimal basePrice;

    @NotNull
    LocalDateTime effectiveFromAt;

    LocalDateTime effectiveToAt;
}
