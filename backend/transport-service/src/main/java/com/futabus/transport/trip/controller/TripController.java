package com.futabus.transport.trip.controller;

import com.futabus.transport.route.dto.response.ApiResponse;
import com.futabus.transport.trip.dto.request.TripCreationRequest;
import com.futabus.transport.trip.dto.response.TripResponse;
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
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripController {
    TripService tripService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> create(
            @Valid @RequestBody TripCreationRequest request
    ) {
        TripResponse trip = tripService.create(request);

        return ResponseEntity
                .created(URI.create("/api/trips/" + trip.getId()))
                .body(ApiResponse.<TripResponse>builder()
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
