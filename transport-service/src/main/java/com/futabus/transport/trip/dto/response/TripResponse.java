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

    LocalDateTime estimatedDepartureAt;

    LocalDateTime estimatedArrivalAt;

    LocalDateTime actualDepartureAt;

    LocalDateTime actualArrivalAt;

    BigDecimal ticketPrice;

    int capacitySnapshot;

    TripStatus status;

    SaleStatus saleStatus;
}
