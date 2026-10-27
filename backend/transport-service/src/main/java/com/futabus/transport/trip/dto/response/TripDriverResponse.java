package com.futabus.transport.trip.dto.response;

import com.futabus.transport.trip.enums.RoleDriver;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripDriverResponse {
    Long id;

    Long tripId;

    Long driverId;

    RoleDriver role;
}
