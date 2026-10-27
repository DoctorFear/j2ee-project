// API giữ chỗ, thanh toán và tra cứu đơn của khách hàng
package com.futa.booking.controller;

import com.futa.booking.dto.*;
import com.futa.booking.entity.Booking;
import com.futa.booking.service.*;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
  private final BookingService bookings;
  private final PaymentService payments;

  // Tiếp nhận yêu cầu giữ ghế của khách hàng
  @PostMapping("/hold")
  public ApiResponse<?> hold(
      @Valid @RequestBody HoldRequest request, @AuthenticationPrincipal Jwt jwt) {
    return response(bookings.hold(request, jwt.getSubject()));
  }

  // Tra cứu đơn thuộc tài khoản đang đăng nhập
  @GetMapping("/{id}")
  public ApiResponse<?> get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
    return response(bookings.owned(id, jwt.getSubject()));
  }

  // Hủy giữ chỗ chưa hoàn tất thanh toán
  @DeleteMapping("/{id}/hold")
  public ApiResponse<?> release(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
    bookings.release(id, jwt.getSubject());
    return ApiResponse.of(true);
  }

  // Trả liên kết thanh toán và số tiền do máy chủ xác định
  @PostMapping("/{id}/pay")
  public ApiResponse<?> pay(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
    var payment = payments.start(id, jwt.getSubject());
    return ApiResponse.of(
        Map.of(
            "id",
            payment.getId(),
            "payUrl",
            payment.getPayUrl(),
            "qrCodeUrl",
            payment.getQrCodeUrl(),
            "amount",
            payment.getAmount()));
  }

  // Trả trạng thái đơn và dữ liệu để khôi phục phiên giữ ghế
  private ApiResponse<?> response(Booking booking) {
    return ApiResponse.of(
        Map.ofEntries(
            Map.entry("id", booking.getId()),
            Map.entry("tripCode", booking.getTripCode()),
            Map.entry("status", booking.getStatus()),
            Map.entry("expiresAt", booking.getExpiresAt()),
            Map.entry("serverTime", Instant.now()),
            Map.entry("amount", booking.getAmount()),
            Map.entry("fare", booking.getFare()),
            Map.entry("seatCodes", booking.getSeatCodes().split(",")),
            Map.entry("originId", booking.getOriginId()),
            Map.entry("destinationId", booking.getDestinationId()),
            Map.entry(
                "contact",
                new TicketView.Contact(
                    booking.getContactName(),
                    booking.getContactPhone(),
                    booking.getContactEmail()))));
  }
}
