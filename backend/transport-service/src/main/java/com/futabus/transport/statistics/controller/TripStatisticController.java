package com.futabus.transport.statistics.controller;

import com.futabus.transport.common.response.ApiResponse;
import com.futabus.transport.statistics.dto.TripStatisticIngestResult;
import com.futabus.transport.statistics.dto.request.TripStatisticIngestRequest;
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
@RequestMapping("/trip-statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TripStatisticController {
    TripStatisticService tripStatisticService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripStatisticResponse>> create(
            @Valid @RequestBody TripStatisticIngestRequest request
    ) {
        TripStatisticIngestResult result = tripStatisticService.ingest(request);
        TripStatisticResponse statistic = result.statistic();

        var body = ApiResponse.<TripStatisticResponse>builder()
                .success(true)
                .data(statistic)
                .build();

        if (result.created()) {
            return ResponseEntity
                    .created(URI.create("/trip-statistics/" + statistic.getTripId()))
                    .body(body);
        }

        return ResponseEntity.ok(body);
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

}
