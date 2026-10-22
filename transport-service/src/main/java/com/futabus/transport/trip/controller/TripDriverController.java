package com.futabus.transport.trip.controller;

import com.futabus.transport.route.dto.response.ApiResponse;
import com.futabus.transport.trip.dto.request.TripDriverCreationRequest;
import com.futabus.transport.trip.dto.response.TripDriverResponse;
import com.futabus.transport.trip.service.TripDriverService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/trip-drivers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripDriverController {
    TripDriverService tripDriverService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripDriverResponse>> create(
            @Valid @RequestBody TripDriverCreationRequest request
    ) {
        TripDriverResponse tripDriver = tripDriverService.create(request);

        return ResponseEntity
                .created(URI.create("/api/trip-drivers/" + tripDriver.getId()))
                .body(ApiResponse.<TripDriverResponse>builder()
                        .success(true)
                        .data(tripDriver)
                        .build()
                );
    }

    @GetMapping
    public ApiResponse<List<TripDriverResponse>> findAll() {
        List<TripDriverResponse> tripDriverResponses = tripDriverService.findAll();

        return ApiResponse.<List<TripDriverResponse>>builder()
                .success(true)
                .data(tripDriverResponses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TripDriverResponse> findById(@PathVariable Long id) {
        TripDriverResponse tripDriverResponse = tripDriverService.findById(id);

        return ApiResponse.<TripDriverResponse>builder()
                .success(true)
                .data(tripDriverResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripDriverService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
