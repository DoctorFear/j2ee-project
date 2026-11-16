// API cung cấp lịch trình ca trực và cập nhật trạng thái chuyến xe cho tài xế
package com.futabus.transport.trip.controller;

import com.futabus.transport.trip.dto.response.DriverTripScheduleResponse;
import com.futabus.transport.trip.service.DriverTripService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/trips")
@RequiredArgsConstructor
public class DriverTripController {

    private final DriverTripService driverTripService;

    /**
     * GET /api/driver/trips/profile: Lấy hồ sơ tài xế đồng bộ từ danh sách tài xế của Admin
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getDriverProfile(
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String phone,
            @RequestHeader(value = "X-User-Id", required = false) Long headerDriverId
    ) {
        Long targetDriverId = driverId != null ? driverId : headerDriverId;
        Map<String, Object> profile = driverTripService.getDriverProfile(targetDriverId, phone);
        return ResponseEntity.ok(Map.of("success", true, "data", profile));
    }

    /**
     * GET /api/driver/trips: Lấy lịch trình ca trực của tài xế
     */
    @GetMapping
    public ResponseEntity<?> getDriverTrips(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String phone,
            @RequestHeader(value = "X-User-Id", required = false) Long headerDriverId
    ) {
        Long targetDriverId = driverId != null ? driverId : headerDriverId;
        List<DriverTripScheduleResponse> trips = driverTripService.getDriverTrips(targetDriverId, phone, date);
        return ResponseEntity.ok(Map.of("success", true, "data", trips));
    }

    /**
     * PUT /api/driver/trips/{code}/status: Tài xế cập nhật trạng thái chuyến (RUNNING / COMPLETED)
     */
    @PutMapping("/{code}/status")
    public ResponseEntity<?> updateTripStatus(
            @PathVariable("code") String tripCode,
            @RequestParam String status
    ) {
        try {
            DriverTripScheduleResponse response = driverTripService.updateTripStatus(tripCode, status);
            return ResponseEntity.ok(Map.of("success", true, "data", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
