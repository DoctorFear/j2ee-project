package com.futabus.transport.route.controller;

import com.futabus.transport.route.dto.request.RouteCreationRequest;
import com.futabus.transport.route.dto.response.ApiResponse;
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
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RouteController {
    RouteService routeService;

    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> create(
            @Valid @RequestBody RouteCreationRequest request
    ) {
        RouteResponse route = routeService.create(request);

        URI routeURI = URI.create("/api/routes/" + route.getId());

        return ResponseEntity.created(routeURI)
                .body(ApiResponse.<RouteResponse>builder()
                        .success(true)
                        .data(route)
                        .build()
                );
    }

    @GetMapping
    public ApiResponse<List<RouteResponse>> findAll() {
        return ApiResponse.<List<RouteResponse>>builder()
                .success(true)
                .data(routeService.findAll())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RouteResponse> findById(@PathVariable Long id) {
        return ApiResponse.<RouteResponse>builder()
                .success(true)
                .data(routeService.findById(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        routeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
