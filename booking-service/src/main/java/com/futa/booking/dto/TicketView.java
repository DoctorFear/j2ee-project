// Thông tin vé và hành trình đã chốt khi thanh toán
package com.futa.booking.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketView(
    String code,
    String bookingId,
    String tripCode,
    String seatCode,
    String originId,
    String destinationId,
    Contact passenger,
    BigDecimal fare,
    BigDecimal fee,
    String paymentMethod,
    String status,
    String qrToken,
    Journey trip,
    Instant pickupAt,
    Instant arrivalAt) {
  public record Contact(String name, String phone, String email) {}
}
