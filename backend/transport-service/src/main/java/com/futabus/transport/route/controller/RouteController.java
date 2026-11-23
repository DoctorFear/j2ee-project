package com.futabus.transport.route.controller;

import com.futabus.transport.common.response.ApiResponse;
import com.futabus.transport.route.dto.request.RouteCreationRequest;
import com.futabus.transport.route.dto.request.RouteUpdateRequest;
import com.futabus.transport.route.dto.response.RouteResponse;
import com.futabus.transport.route.service.RouteService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RouteController {
    RouteService routeService;

    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> create(
            @Valid @RequestBody RouteCreationRequest request
    ) {
        RouteResponse route = routeService.create(request);

        return ResponseEntity
                .created(URI.create("/routes/" + route.getId()))
                .body(
                        ApiResponse.<RouteResponse>builder()
                                .success(true)
                                .data(route)
                                .build()
                );
    }

    @GetMapping
    public ApiResponse<List<RouteResponse>> findAll() {
        List<RouteResponse> routeResponses = routeService.findAll();

        return ApiResponse.<List<RouteResponse>>builder()
                .success(true)
                .data(routeResponses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RouteResponse> findById(@PathVariable Long id) {
        RouteResponse routeResponse = routeService.findById(id);

        return ApiResponse.<RouteResponse>builder()
                .success(true)
                .data(routeResponse)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<RouteResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RouteUpdateRequest request
    ) {
        RouteResponse routeResponse = routeService.update(id, request);

        return ApiResponse.<RouteResponse>builder()
                .success(true)
                .data(routeResponse)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
