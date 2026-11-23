package com.futabus.transport.trip.controller;

import com.futabus.transport.common.response.ApiResponse;
import com.futabus.transport.trip.dto.response.DriverTripScheduleResponse;
import com.futabus.transport.trip.service.DriverTripService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/driver/trips")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DriverTripController {
    DriverTripService driverTripService;

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getDriverProfile(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String phone,
            @RequestHeader(value = "X-User-Id", required = false) Long headerDriverId
    ) {
        Long targetDriverId = driverId != null ? driverId : headerDriverId;
        Map<String, Object> profile = driverTripService.getDriverProfile(targetDriverId, phone);

        return ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(profile)
                .build();
    }

    @GetMapping
    public ApiResponse<List<DriverTripScheduleResponse>> getDriverTrips(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String phone,
            @RequestHeader(value = "X-User-Id", required = false) Long headerDriverId
    ) {
        Long targetDriverId = driverId != null ? driverId : headerDriverId;
        List<DriverTripScheduleResponse> trips = driverTripService.getDriverTrips(targetDriverId, phone, date);

        return ApiResponse.<List<DriverTripScheduleResponse>>builder()
                .success(true)
                .data(trips)
                .build();
    }

    @PutMapping("/{code}/status")
    public ApiResponse<DriverTripScheduleResponse> updateTripStatus(
            @PathVariable("code") String tripCode,
            @RequestParam String status
    ) {
        DriverTripScheduleResponse response = driverTripService.updateTripStatus(tripCode, status);

        return ApiResponse.<DriverTripScheduleResponse>builder()
                .success(true)
                .data(response)
                .build();
    }
}
