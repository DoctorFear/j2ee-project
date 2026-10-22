package com.futabus.transport.resource.controller;

import com.futabus.transport.resource.dto.request.VehicleCreationRequest;
import com.futabus.transport.resource.dto.response.VehicleResponse;
import com.futabus.transport.resource.service.VehicleService;
import com.futabus.transport.route.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
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
                .created(URI.create("/api/vehicles/" + vehicle.getId()))
                .body(ApiResponse.<VehicleResponse>builder()
                        .success(true)
                        .data(vehicle)
                        .build()
                );
    }

    @GetMapping
    public ApiResponse<List<VehicleResponse>> findAll() {
        List<VehicleResponse> vehicleResponses = vehicleService.findAll();

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
