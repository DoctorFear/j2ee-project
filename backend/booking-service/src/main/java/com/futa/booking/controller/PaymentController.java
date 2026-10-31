// Tiếp nhận thông báo thanh toán có chữ ký từ MoMo
package com.futa.booking.controller;

import com.futa.booking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService payments;

  // Tiếp nhận thông báo Webhook thanh toán từ PayOS
  @PostMapping({"/payos/webhook", "/api/bookings/payments/payos/webhook"})
  public ResponseEntity<Void> notify(@RequestBody JsonNode body) {
    payments.notifyPayOS(body);
    return ResponseEntity.noContent().build();
  }
}
