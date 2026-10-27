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

  @PostMapping("/api/bookings/payments/momo/ipn")
  public ResponseEntity<Void> notify(@RequestBody JsonNode body) {
    payments.notify(body);
    return ResponseEntity.noContent().build();
  }
}
