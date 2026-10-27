// Tạo thanh toán và phát hành vé theo kết quả đã xác minh
package com.futa.booking.service;

import com.futa.booking.client.MomoClient;
import com.futa.booking.entity.*;
import com.futa.booking.exception.BusinessException;
import com.futa.booking.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private final BookingRepository bookings;
  private final PaymentRepository payments;
  private final SeatReservationRepository seats;
  private final InventoryService inventory;
  private final TicketService tickets;
  private final MomoClient momo;
  private final jakarta.persistence.EntityManager entityManager;

  // Tái sử dụng phiên đang có để tránh tạo giao dịch thanh toán trùng
  @Transactional(noRollbackFor = BusinessException.class)
  public Payment start(String bookingId, String userId) {
    Booking before =
        bookings
            .findById(bookingId)
            .filter(b -> b.getUserId().equals(userId))
            .orElseThrow(() -> error("Không tìm thấy đơn đặt vé."));
    inventory.lock(before.getTripCode());
    Booking booking = bookings.lock(bookingId).orElseThrow();
    entityManager.refresh(booking, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (!booking.getStatus().equals("HELD") || !booking.getExpiresAt().isAfter(Instant.now()))
      throw error("Phiên giữ chỗ đã hết hạn hoặc không thể thanh toán.");
    Payment payment =
        payments
            .findByBookingId(bookingId)
            .orElseGet(
                () -> {
                  var p = new Payment();
                  p.setId("PAY-" + bookingId);
                  p.setRequestId("REQ-" + bookingId);
                  p.setBookingId(bookingId);
                  p.setAmount(booking.getAmount());
                  p.setStatus("PENDING");
                  return p;
                });
    if (payment.getPayUrl() != null) return payment;
    payments.saveAndFlush(payment);
    var result = momo.create(payment);
    payment.setPayUrl(result.path("payUrl").asText());
    payment.setQrCodeUrl(result.path("qrCodeUrl").asText(""));
    return payments.save(payment);
  }

  // Ghi nhận callback một lần và cách ly tiền về sau khi ghế đã hết hạn
  @Transactional
  public void notify(JsonNode body) {
    momo.verify(body);
    record(body);
  }

  // Đối chiếu trực tiếp với MoMo khi callback chưa về tới hệ thống
  @Transactional
  public void reconcile(String id) {
    var payment = payments.findById(id).orElseThrow();
    if (!payment.getStatus().equals("PENDING")) return;
    record(momo.query(payment));
  }

  private void record(JsonNode body) {
    var before =
        payments
            .findById(body.path("orderId").asText())
            .orElseThrow(() -> error("Không tìm thấy giao dịch."));
    var oldBooking = bookings.findById(before.getBookingId()).orElseThrow();
    inventory.lock(oldBooking.getTripCode());
    var booking = bookings.lock(oldBooking.getId()).orElseThrow();
    var payment = payments.lock(before.getId()).orElseThrow();
    entityManager.refresh(booking, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    entityManager.refresh(payment, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (!payment.getRequestId().equals(body.path("requestId").asText()))
      throw error("Thông tin giao dịch không khớp.");
    if (payment.getStatus().equals("SUCCESS") || payment.getStatus().equals("REVIEW_REQUIRED")) {
      if (!body.path("transId").asText().equals(payment.getProviderTxnId()))
        throw error("Mã giao dịch không khớp.");
      return;
    }
    int result = body.path("resultCode").asInt(-1);
    if (result != 0) {
      if (java.util.Set.of(1001, 1002, 1003, 1004, 1005, 1006, 1007).contains(result)) {
        payment.setStatus("FAILED");
        if (booking.getStatus().equals("HELD")) {
          booking.setStatus("CANCELLED");
          seats.deleteByBookingIdAndStatus(booking.getId(), "HELD");
        }
      }
      return;
    }
    if (!body.has("amount")
        || payment.getAmount().compareTo(new BigDecimal(body.path("amount").asText())) != 0)
      throw error("Số tiền giao dịch không khớp.");
    if (body.path("transId").asLong(0) <= 0) throw error("Mã giao dịch không hợp lệ.");
    payment.setProviderTxnId(body.path("transId").asText());
    payment.setCompletedAt(Instant.now());
    var held = seats.findByBookingId(booking.getId());
    if (!booking.getStatus().equals("HELD")
        || !booking.getExpiresAt().isAfter(Instant.now())
        || held.size() != booking.getSeatCodes().split(",").length) {
      payment.setStatus("REVIEW_REQUIRED");
      booking.setStatus("PAYMENT_REVIEW");
      return;
    }
    payment.setStatus("SUCCESS");
    booking.setStatus("PAID");
    held.forEach(seat -> seat.setStatus("SOLD"));
    tickets.issue(booking);
  }

  private BusinessException error(String message) {
    return new BusinessException(HttpStatus.CONFLICT, "PAYMENT_CONFLICT", message);
  }
}
