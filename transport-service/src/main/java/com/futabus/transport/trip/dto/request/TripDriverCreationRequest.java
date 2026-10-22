package com.futabus.transport.trip.dto.request;

import com.futabus.transport.trip.enums.RoleDriver;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripDriverCreationRequest {
    @NotNull
    @Positive
    Long tripId;

    @NotNull
    @Positive
    Long driverId;

    @NotNull
    RoleDriver role;
}
