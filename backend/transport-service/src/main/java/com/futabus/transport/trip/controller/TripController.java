package com.futabus.transport.trip.controller;

import com.futabus.transport.common.response.ApiResponse;
import com.futabus.transport.trip.dto.request.TripCreationRequest;
import com.futabus.transport.trip.dto.request.TripOperations;
import com.futabus.transport.trip.dto.request.TripUpdateRequest;
import com.futabus.transport.trip.dto.response.TripResponse;
import com.futabus.transport.trip.dto.response.TripDriverResponse;
import com.futabus.transport.trip.service.TripDriverService;
import com.futabus.transport.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripController {
    TripService tripService;
    TripDriverService tripDriverService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> create(
            @Valid @RequestBody TripCreationRequest request
    ) {
        TripResponse trip = tripService.create(request);

        return ResponseEntity
                .created(URI.create("/trips/" + trip.getId()))
                .body(
                        ApiResponse.<TripResponse>builder()
                                .success(true)
                                .data(trip)
                                .build()
                );
    }

    @GetMapping
    public ApiResponse<List<TripResponse>> findAll() {
        List<TripResponse> tripResponses = tripService.findAll();

        return ApiResponse.<List<TripResponse>>builder()
                .success(true)
                .data(tripResponses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TripResponse> findById(@PathVariable Long id) {
        TripResponse tripResponse = tripService.findById(id);

        return ApiResponse.<TripResponse>builder()
                .success(true)
                .data(tripResponse)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TripResponse> updateDraft(
            @PathVariable Long id,
            @Valid @RequestBody TripUpdateRequest request
    ) {
        return toTripApiResponse(tripService.updateDraft(id, request));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<TripResponse> publish(@PathVariable Long id) {
        return toTripApiResponse(tripService.publish(id));
    }

    @PutMapping("/{id}/vehicle")
    public ApiResponse<TripResponse> assignVehicle(
            @PathVariable Long id,
            @Valid @RequestBody TripOperations.Vehicle request
    ) {
        return toTripApiResponse(tripService.assignVehicle(id, request.vehicleId()));
    }

    @PutMapping("/{id}/primary-driver")
    public ApiResponse<TripDriverResponse> replacePrimary(
            @PathVariable Long id,
            @Valid @RequestBody TripOperations.PrimaryDriver request
    ) {
        TripDriverResponse tripDriverResponse = tripDriverService.replacePrimary(id, request.driverId());

        return toTripDriverApiResponse(tripDriverResponse);
    }

    @PutMapping("/{id}/sale-status")
    public ApiResponse<TripResponse> changeSaleStatus(
            @PathVariable Long id,
            @Valid @RequestBody TripOperations.Sale request
    ) {
        return toTripApiResponse(tripService.changeSaleStatus(id, request.saleStatus()));
    }

    @PutMapping("/{id}/estimate")
    public ApiResponse<TripResponse> changeEstimate(
            @PathVariable Long id,
            @RequestBody TripOperations.Estimate request
    ) {
        return toTripApiResponse(tripService.changeEstimate(
                id,
                request.estimatedDepartureAt(),
                request.estimatedArrivalAt()
        ));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<TripResponse> cancel(
            @PathVariable Long id,
            @Valid @RequestBody TripOperations.Cancellation request
    ) {
        return toTripApiResponse(tripService.cancel(id, request.reason()));
    }

    private ApiResponse<TripResponse> toTripApiResponse(TripResponse trip) {
        return ApiResponse.<TripResponse>builder()
                .success(true)
                .data(trip)
                .build();
    }

    private ApiResponse<TripDriverResponse> toTripDriverApiResponse(TripDriverResponse tripDriver) {
        return ApiResponse.<TripDriverResponse>builder()
                .success(true)
                .data(tripDriver)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
