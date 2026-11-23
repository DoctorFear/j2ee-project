package com.futabus.transport.route.controller;

import com.futabus.transport.common.response.ApiResponse;
import com.futabus.transport.route.dto.request.StationAddressRequest;
import com.futabus.transport.route.dto.request.StationCreationRequest;
import com.futabus.transport.route.dto.request.StationUpdateRequest;
import com.futabus.transport.route.dto.response.StationAddressResponse;
import com.futabus.transport.route.dto.response.StationResponse;
import com.futabus.transport.route.service.StationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/stations")
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
                .created(URI.create("/stations/" + station.getId()))
                .body(
                        ApiResponse.<StationResponse>builder()
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
    public ApiResponse<StationResponse> findById(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDateTime at
    ) {
        StationResponse stationResponse = stationService.findById(id, at);

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

    @PostMapping("/{id}/addresses")
    public ResponseEntity<ApiResponse<StationAddressResponse>> addAddress(
            @PathVariable Long id,
            @Valid @RequestBody StationAddressRequest request
    ) {
        StationAddressResponse address = stationService.addAddress(id, request);

        return ResponseEntity
                .status(201)
                .body(
                        ApiResponse.<StationAddressResponse>builder()
                                .success(true)
                                .data(address)
                                .build()
                );
    }

    @GetMapping("/{id}/addresses")
    public ApiResponse<List<StationAddressResponse>> findAddresses(@PathVariable Long id) {
        List<StationAddressResponse> addresses = stationService.findAddresses(id);

        return ApiResponse.<List<StationAddressResponse>>builder()
                .success(true)
                .data(addresses)
                .build();
    }
}
