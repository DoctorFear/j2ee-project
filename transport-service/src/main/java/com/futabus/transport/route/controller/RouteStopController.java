package com.futabus.transport.route.controller;

import com.futabus.transport.route.dto.request.RouteStopCreationRequest;
import com.futabus.transport.route.dto.response.ApiResponse;
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
@RequestMapping("/api/route-stops")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RouteStopController {
    RouteStopService routeStopService;

    @PostMapping
    public ResponseEntity<ApiResponse<RouteStopResponse>> create(
            @Valid @RequestBody RouteStopCreationRequest request) {
        RouteStopResponse routeStop = routeStopService.create(request);
        return ResponseEntity.created(URI.create("/api/route-stops/" + routeStop.getId()))
                .body(ApiResponse.<RouteStopResponse>builder().success(true).data(routeStop).build());
    }

    @GetMapping
    public ApiResponse<List<RouteStopResponse>> findAll(@RequestParam(required = false) Long routeId) {
        return ApiResponse.<List<RouteStopResponse>>builder()
                .success(true).data(routeStopService.findAll(routeId)).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RouteStopResponse> findById(@PathVariable Long id) {
        return ApiResponse.<RouteStopResponse>builder()
                .success(true).data(routeStopService.findById(id)).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeStopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
