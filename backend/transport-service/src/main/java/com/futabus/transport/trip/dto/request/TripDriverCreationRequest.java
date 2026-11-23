package com.futabus.transport.trip.dto.request;

import com.futabus.transport.trip.enums.RoleDriver;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
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
