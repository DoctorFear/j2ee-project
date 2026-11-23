package com.futabus.transport.route.controller;

import com.futabus.transport.common.response.ApiResponse;
import com.futabus.transport.route.dto.request.RouteStopCreationRequest;
import com.futabus.transport.route.dto.request.RouteStopUpdateRequest;
import com.futabus.transport.route.dto.response.RouteStopResponse;
import com.futabus.transport.route.service.RouteStopService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/route-stops")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RouteStopController {
    RouteStopService routeStopService;

    @PostMapping
    public ResponseEntity<ApiResponse<RouteStopResponse>> create(
            @Valid @RequestBody RouteStopCreationRequest request
    ) {
        RouteStopResponse routeStop = routeStopService.create(request);

        return ResponseEntity
                .created(URI.create("/route-stops/" + routeStop.getId()))
                .body(
                        ApiResponse.<RouteStopResponse>builder()
                                .success(true)
                                .data(routeStop)
                                .build()
                );
    }

    @GetMapping
    public ApiResponse<List<RouteStopResponse>> findAll(@RequestParam(required = false) Long routeId) {
        List<RouteStopResponse> routeStops = routeStopService.findAll(routeId);

        return ApiResponse.<List<RouteStopResponse>>builder()
                .success(true)
                .data(routeStops)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RouteStopResponse> findById(@PathVariable Long id) {
        RouteStopResponse routeStop = routeStopService.findById(id);

        return ApiResponse.<RouteStopResponse>builder()
                .success(true)
                .data(routeStop)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<RouteStopResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RouteStopUpdateRequest request
    ) {
        RouteStopResponse routeStop = routeStopService.update(id, request);

        return ApiResponse.<RouteStopResponse>builder()
                .success(true)
                .data(routeStop)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeStopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
