// API danh sách đón/trả khách, tra cứu và xác thực soát vé cho tài xế
package com.futa.booking.controller;

import com.futa.booking.dto.driver.*;
import com.futa.booking.service.DriverCheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/trips")
@RequiredArgsConstructor
public class DriverCheckInController {

    private final DriverCheckInService checkInService;

    /**
     * GET /api/driver/trips/{code}/passengers: Lấy danh sách hành khách theo chuyến
     */
    @GetMapping("/{code}/passengers")
    public ResponseEntity<?> getPassengers(@PathVariable("code") String tripCode) {
        List<DriverPassengerResponse> passengers = checkInService.getTripPassengers(tripCode);
        return ResponseEntity.ok(Map.of("success", true, "data", passengers));
    }

    /**
     * GET /api/driver/trips/{code}/summary: Lấy tiến độ lên xe và danh sách hành khách
     */
    @GetMapping("/{code}/summary")
    public ResponseEntity<?> getTripSummary(@PathVariable("code") String tripCode) {
        DriverTripSummaryResponse summary = checkInService.getTripSummary(tripCode);
        return ResponseEntity.ok(Map.of("success", true, "data", summary));
    }

    /**
     * GET /api/driver/trips/{code}/tickets/{ticketCode}: Tra cứu thông tin vé
     */
    @GetMapping("/{code}/tickets/{ticketCode}")
    public ResponseEntity<?> lookupTicket(
            @PathVariable("code") String tripCode,
            @PathVariable("ticketCode") String ticketCode
    ) {
        try {
            DriverPassengerResponse ticket = checkInService.lookupTicket(tripCode, ticketCode);
            return ResponseEntity.ok(Map.of("success", true, "data", ticket));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /api/driver/trips/{code}/check-in: Quét mã QR, mã vé hoặc 4 số cuối SĐT để soát vé
     */
    @PostMapping("/{code}/check-in")
    public ResponseEntity<?> checkIn(
            @PathVariable("code") String tripCode,
            @RequestBody CheckInRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "driver-system") String driverId
    ) {
        try {
            CheckInResponse response = checkInService.checkIn(tripCode, request, driverId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * POST /api/driver/trips/{code}/passengers/{ticketCode}/toggle: Đổi trạng thái 1 chạm thủ công
     */
    @PostMapping("/{code}/passengers/{ticketCode}/toggle")
    public ResponseEntity<?> toggleBoarded(
            @PathVariable("code") String tripCode,
            @PathVariable("ticketCode") String ticketCode,
            @RequestHeader(value = "X-User-Id", defaultValue = "driver-system") String driverId
    ) {
        try {
            DriverPassengerResponse res = checkInService.toggleBoarded(tripCode, ticketCode, driverId);
            return ResponseEntity.ok(Map.of("success", true, "data", res));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
