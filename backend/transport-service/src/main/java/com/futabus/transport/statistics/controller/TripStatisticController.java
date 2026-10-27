package com.futabus.transport.statistics.controller;

import com.futabus.transport.route.dto.response.ApiResponse;
import com.futabus.transport.statistics.dto.request.TripStatisticCreationRequest;
import com.futabus.transport.statistics.dto.response.TripStatisticResponse;
import com.futabus.transport.statistics.service.TripStatisticService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/trip-statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripStatisticController {
    TripStatisticService tripStatisticService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripStatisticResponse>> create(
            @Valid @RequestBody TripStatisticCreationRequest request
    ) {
        TripStatisticResponse tripStatistic = tripStatisticService.create(request);

        String pathURI = "/api/trip-statistics/" + tripStatistic.getTripId();

        return ResponseEntity
                .created(URI.create(pathURI))
                .body(ApiResponse.<TripStatisticResponse>builder()
                        .success(true)
                        .data(tripStatistic)
                        .build()
                );
    }

    @GetMapping
    public ApiResponse<List<TripStatisticResponse>> findAll() {
        List<TripStatisticResponse> tripStatisticResponses = tripStatisticService.findAll();

        return ApiResponse.<List<TripStatisticResponse>>builder()
                .success(true)
                .data(tripStatisticResponses)
                .build();
    }

    @GetMapping("/{tripId}")
    public ApiResponse<TripStatisticResponse> findById(@PathVariable Long tripId) {
        TripStatisticResponse tripStatisticResponse = tripStatisticService.findById(tripId);

        return ApiResponse.<TripStatisticResponse>builder()
                .success(true)
                .data(tripStatisticResponse)
                .build();
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<Void> delete(@PathVariable Long tripId) {
        tripStatisticService.delete(tripId);

        return ResponseEntity.noContent().build();
    }
}
