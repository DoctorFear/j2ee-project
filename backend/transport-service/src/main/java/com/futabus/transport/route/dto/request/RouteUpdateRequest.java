package com.futabus.transport.route.dto.request;

import com.futabus.transport.route.enums.RouteStatus;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteUpdateRequest {
    @NotBlank
    @Size(max = 30)
    String code;

    @Positive
    @NotNull
    Integer versionNo = 1;

    @NotBlank
    @Size(max = 200)
    String name;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 15, fraction = 0)
    BigDecimal basePrice;

    @NotNull
    LocalDateTime effectiveFromAt;

    LocalDateTime effectiveToAt;

    @NotNull
    RouteStatus status;
}
