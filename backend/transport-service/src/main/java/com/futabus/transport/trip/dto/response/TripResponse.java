package com.futabus.transport.trip.dto.response;

import com.futabus.transport.trip.enums.SaleStatus;
import com.futabus.transport.trip.enums.TripStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripResponse {
    Long id;

    String code;

    Long routeId;

    Long vehicleId;

    LocalDateTime departureAt;

    LocalDateTime arrivalAt;

    LocalDateTime publishedAt;

    LocalDateTime estimatedDepartureAt;

    LocalDateTime estimatedArrivalAt;

    LocalDateTime actualDepartureAt;

    LocalDateTime actualArrivalAt;

    LocalDateTime cancelledAt;

    String cancelReason;

    BigDecimal ticketPrice;

    Integer capacitySnapshot;

    TripStatus status;

    SaleStatus saleStatus;
}
