package com.futabus.transport.route.dto.response;

import com.futabus.transport.route.enums.RouteStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteResponse {
    Long id;

    String code;

    int versionNo;

    String name;

    BigDecimal basePrice;

    LocalDateTime effectiveFromAt;

    LocalDateTime effectiveToAt;

    RouteStatus status;
}
