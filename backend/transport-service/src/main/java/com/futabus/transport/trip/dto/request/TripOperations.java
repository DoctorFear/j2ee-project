package com.futabus.transport.trip.dto.request;

import com.futabus.transport.trip.enums.SaleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class TripOperations {
    private TripOperations() {
    }

    public record Vehicle(
            @NotNull @Positive Long vehicleId
    ) {
    }

    public record PrimaryDriver(
            @NotNull @Positive Long driverId
    ) {
    }

    public record Sale(
            @NotNull SaleStatus saleStatus
    ) {
    }

    public record Estimate(
            LocalDateTime estimatedDepartureAt,
            LocalDateTime estimatedArrivalAt
    ) {
    }

    public record Cancellation(
            @NotBlank @Size(max = 255) String reason
    ) {
    }
}
