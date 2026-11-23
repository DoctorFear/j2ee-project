package com.futabus.transport.resource.controller;

import com.futabus.transport.common.response.ApiResponse;
import com.futabus.transport.resource.dto.request.DriverCreationRequest;
import com.futabus.transport.resource.dto.request.DriverUpdateRequest;
import com.futabus.transport.resource.dto.response.DriverResponse;
import com.futabus.transport.resource.enums.DriverStatus;
import com.futabus.transport.resource.service.DriverService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DriverController {
    DriverService driverService;

    @PostMapping
    public ResponseEntity<ApiResponse<DriverResponse>> create(
            @Valid @RequestBody DriverCreationRequest request
    ) {
        DriverResponse driver = driverService.create(request);

        return ResponseEntity
                .created(URI.create("/drivers/" + driver.getId()))
                .body(
                        ApiResponse.<DriverResponse>builder()
                                .success(true)
                                .data(driver)
                                .build()
                );
    }

    @GetMapping
    public ApiResponse<List<DriverResponse>> findAll(
            @RequestParam(required = false) DriverStatus status
    ) {
        List<DriverResponse> driverResponses = driverService.findAll(status);

        return ApiResponse.<List<DriverResponse>>builder()
                .success(true)
                .data(driverResponses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DriverResponse> findById(@PathVariable Long id) {
        DriverResponse driverResponse = driverService.findById(id);

        return ApiResponse.<DriverResponse>builder()
                .success(true)
                .data(driverResponse)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<DriverResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DriverUpdateRequest request
    ) {
        DriverResponse driverResponse = driverService.update(id, request);

        return ApiResponse.<DriverResponse>builder()
                .success(true)
                .data(driverResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        driverService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
