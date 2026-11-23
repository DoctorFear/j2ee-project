package com.futabus.transport.resource.controller;

import com.futabus.transport.common.response.ApiResponse;
import com.futabus.transport.resource.dto.request.VehicleCreationRequest;
import com.futabus.transport.resource.dto.request.VehicleStatusRequest;
import com.futabus.transport.resource.dto.request.VehicleUpdateRequest;
import com.futabus.transport.resource.dto.response.VehicleResponse;
import com.futabus.transport.resource.enums.ServiceType;
import com.futabus.transport.resource.enums.VehicleStatus;
import com.futabus.transport.resource.service.VehicleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VehicleController {
    VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> create(
            @Valid @RequestBody VehicleCreationRequest request
    ) {
        VehicleResponse vehicle = vehicleService.create(request);

        return ResponseEntity
                .created(URI.create("/vehicles/" + vehicle.getId()))
                .body(
                        ApiResponse.<VehicleResponse>builder()
                                .success(true)
                                .data(vehicle)
                                .build()
                );
    }

    @GetMapping
    public ApiResponse<List<VehicleResponse>> findAll(
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) ServiceType type
    ) {
        List<VehicleResponse> vehicleResponses = vehicleService.findAll(status, type);

        return ApiResponse.<List<VehicleResponse>>builder()
                .success(true)
                .data(vehicleResponses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VehicleResponse> findById(@PathVariable Long id) {
        VehicleResponse vehicleResponse = vehicleService.findById(id);

        return ApiResponse.<VehicleResponse>builder()
                .success(true)
                .data(vehicleResponse)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<VehicleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VehicleUpdateRequest request
    ) {
        VehicleResponse vehicleResponse = vehicleService.update(id, request);

        return ApiResponse.<VehicleResponse>builder()
                .success(true)
                .data(vehicleResponse)
                .build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<VehicleResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody VehicleStatusRequest request
    ) {
        VehicleResponse vehicleResponse = vehicleService.changeStatus(id, request.status());

        return ApiResponse.<VehicleResponse>builder()
                .success(true)
                .data(vehicleResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
