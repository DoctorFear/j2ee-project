package com.futabus.transport.route.controller;

import com.futabus.transport.common.response.ApiResponse;
import com.futabus.transport.route.dto.request.StationSegmentCreationRequest;
import com.futabus.transport.route.dto.request.StationSegmentUpdateRequest;
import com.futabus.transport.route.dto.response.StationSegmentResponse;
import com.futabus.transport.route.service.StationSegmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/stations/segments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StationSegmentController {
    StationSegmentService stationSegmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<StationSegmentResponse>> create(
            @Valid @RequestBody StationSegmentCreationRequest request
    ) {
        StationSegmentResponse result = stationSegmentService.create(request);

        return ResponseEntity
                .created(URI.create("/stations/segments/" + result.id()))
                .body(
                        ApiResponse.<StationSegmentResponse>builder()
                                .success(true)
                                .data(result)
                                .build()
                );
    }

    @GetMapping
    public ApiResponse<List<StationSegmentResponse>> findAll() {
        List<StationSegmentResponse> segmentResponses = stationSegmentService.findAll();

        return ApiResponse.<List<StationSegmentResponse>>builder()
                .success(true)
                .data(segmentResponses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StationSegmentResponse> findById(@PathVariable Long id) {
        StationSegmentResponse segmentResponse = stationSegmentService.findById(id);

        return ApiResponse.<StationSegmentResponse>builder()
                .success(true)
                .data(segmentResponse)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<StationSegmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StationSegmentUpdateRequest request
    ) {
        StationSegmentResponse segmentResponse = stationSegmentService.update(id, request);

        return ApiResponse.<StationSegmentResponse>builder()
                .success(true)
                .data(segmentResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stationSegmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
