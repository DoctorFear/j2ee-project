package com.futabus.transport.route.controller;

import com.futabus.transport.route.dto.request.StationCreationRequest;
import com.futabus.transport.route.dto.request.StationUpdateRequest;
import com.futabus.transport.route.dto.response.ApiResponse;
import com.futabus.transport.route.dto.response.StationResponse;
import com.futabus.transport.route.service.StationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StationController {
    StationService stationService;

    @PostMapping
    public ResponseEntity<ApiResponse<StationResponse>> create(
            @Valid @RequestBody StationCreationRequest request
    ) {
        StationResponse station = stationService.create(request);

        return ResponseEntity
                .created(URI.create("/api/stations/" + station.getId()))
                .body(ApiResponse.<StationResponse>builder()
                        .success(true)
                        .data(station)
                        .build()
                );
    }

    @GetMapping
    public ApiResponse<List<StationResponse>> findAll() {
        List<StationResponse> stationResponses = stationService.findAll();

        return ApiResponse.<List<StationResponse>>builder()
                .success(true)
                .data(stationResponses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StationResponse> findById(@PathVariable Long id) {
        StationResponse stationResponse = stationService.findById(id);

        return ApiResponse.<StationResponse>builder()
                .success(true)
                .data(stationResponse)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<StationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StationUpdateRequest request
    ) {
        StationResponse stationResponse = stationService.update(id, request);

        return ApiResponse.<StationResponse>builder()
                .success(true)
                .data(stationResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
