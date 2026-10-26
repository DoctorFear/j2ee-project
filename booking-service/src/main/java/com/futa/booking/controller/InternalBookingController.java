// API dữ liệu Booking dùng riêng cho các dịch vụ trong hệ thống
package com.futa.booking.controller;

import com.futa.booking.dto.ApiResponse;
import com.futa.booking.service.BookingIntegrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Validated
public class InternalBookingController {
  private final BookingIntegrationService integration;

  // Trả số ghế đã bán theo danh sách mã chuyến
  @PostMapping("/bookings/booked-counts")
  public ApiResponse<?> counts(
      @RequestBody @Valid @Size(min = 1, max = 100) List<@NotBlank String> codes) {
    return ApiResponse.of(
        integration.sales(codes).stream()
            .collect(Collectors.toMap(s -> s.tripCode(), s -> s.bookedCount())));
  }

  // Cung cấp số liệu vé và doanh thu để Transport tổng hợp báo cáo
  @GetMapping("/reports/trip-sales")
  public ApiResponse<?> sales(
      @RequestParam @Size(min = 1, max = 100) List<@NotBlank String> tripCodes) {
    return ApiResponse.of(integration.sales(tripCodes));
  }

  // Cung cấp số khách đón trả để hiển thị lộ trình chuyến
  @GetMapping("/trips/{code}/stop-counts")
  public ApiResponse<?> stops(@PathVariable String code) {
    return ApiResponse.of(integration.stops(code));
  }
}
