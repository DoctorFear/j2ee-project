// Tạo thanh toán và phát hành vé theo kết quả đã xác minh
package com.futa.booking.service;

import com.futa.booking.client.PayOSClient;
import com.futa.booking.entity.*;
import com.futa.booking.exception.BusinessException;
import com.futa.booking.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
  private final BookingRepository bookings;
  private final PaymentRepository payments;
  private final SeatReservationRepository seats;
  private final InventoryService inventory;
  private final TicketService tickets;
  private final PayOSClient payOSClient;
  private final jakarta.persistence.EntityManager entityManager;

  // Khởi tạo phiên thanh toán PayOS VietQR
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
                              p.setBookingId(bookingId);
                              // Sinh orderCode dạng số duy nhất cho PayOS
                              long orderCode = Math.abs(bookingId.hashCode()) * 1000000L + (System.currentTimeMillis() % 1000000L);
                              orderCode = Math.abs(orderCode % 9007199254740991L);
                              p.setRequestId(String.valueOf(orderCode));
                              p.setAmount(booking.getAmount());
                              p.setStatus("PENDING");
                              return p;
                            });
    if (payment.getPayUrl() != null) return payment;
    payments.saveAndFlush(payment);

    long orderCode = Long.parseLong(payment.getRequestId());
    try {
      var result = payOSClient.create(payment, orderCode);
      payment.setPayUrl(result.checkoutUrl());
      payment.setQrCodeUrl(result.qrCode());
    } catch (Exception e) {
      // Nếu bị trùng mã đơn cũ trên PayOS hoặc lỗi chữ ký lượt trước, sinh orderCode mới và thử lại
      // Hủy link của mã cũ (nếu đã lỡ được tạo) để không còn link mồ côi mà khách vẫn thanh toán được
      payOSClient.cancelPaymentLink(orderCode, "Tạo lại mã thanh toán");
      long newOrderCode = System.currentTimeMillis();
      payment.setRequestId(String.valueOf(newOrderCode));
      var result = payOSClient.create(payment, newOrderCode);
      payment.setPayUrl(result.checkoutUrl());
      payment.setQrCodeUrl(result.qrCode());
    }
    return payments.save(payment);
  }

  // Đối chiếu trực tiếp với PayOS khi Webhook chưa về
  @Transactional
  public void reconcile(String id) {
    var payment = payments.findById(id).orElseThrow();
    if (!payment.getStatus().equals("PENDING")) return;

    try {
      // Chỉ chuyển đổi nếu requestId là dạng chuỗi số orderCode hợp lệ của PayOS
      long orderCode = Long.parseLong(payment.getRequestId());
      var info = payOSClient.getPaymentLinkInformation(orderCode);
      if ("PAID".equals(info.status())) {
        recordPayOSSuccess(payment.getBookingId(), info.amount(), String.valueOf(orderCode));
      }
    } catch (NumberFormatException e) {
      // Bỏ qua các bản ghi có requestId không phải dạng số (ví dụ mã REQ-... cũ)
    } catch (Exception e) {
      log.warn("Đối chiếu PayOS thất bại, paymentId={}: {}", id, e.getMessage());
    }
  }

  private void recordPayOS(PayOSClient.PaidEvent data) {
    String orderCodeStr = String.valueOf(data.orderCode());
    recordPayOSSuccessByOrderCode(orderCodeStr, new BigDecimal(data.amount()), data.reference());
  }

  private void recordPayOSSuccessByOrderCode(String orderCodeStr, BigDecimal actualAmount, String providerTxnId) {
    var before = payments.findAll().stream()
            .filter(p -> orderCodeStr.equals(p.getRequestId()))
            .findFirst()
            .orElseThrow(() -> error("Không tìm thấy giao dịch với mã orderCode: " + orderCodeStr));
    recordPayOSSuccess(before.getBookingId(), actualAmount.longValueExact(), providerTxnId);
  }

  private void recordPayOSSuccess(String bookingId, long amountPaid, String providerTxnId) {
    var before = payments.findByBookingId(bookingId).orElseThrow(() -> error("Không tìm thấy giao dịch."));
    var oldBooking = bookings.findById(before.getBookingId()).orElseThrow();
    inventory.lock(oldBooking.getTripCode());
    var booking = bookings.lock(oldBooking.getId()).orElseThrow();
    var payment = payments.lock(before.getId()).orElseThrow();
    entityManager.refresh(booking, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    entityManager.refresh(payment, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);

    if (payment.getStatus().equals("SUCCESS") || payment.getStatus().equals("REVIEW_REQUIRED")) {
      return;
    }

    if (payment.getAmount().compareTo(new BigDecimal(amountPaid)) != 0) {
      throw error("Số tiền giao dịch không khớp.");
    }

    payment.setProviderTxnId(providerTxnId != null ? providerTxnId : payment.getRequestId());
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

  // Tiếp nhận và xử lý Webhook từ PayOS
  @Transactional
  public void notifyPayOS(JsonNode body) {
    if (body == null) {
      return;
    }
    try {
      var data = payOSClient.verifyWebhook(body);
      if (data != null) {
        String orderCodeStr = String.valueOf(data.orderCode());
        // Chỉ xử lý nếu mã đơn tồn tại trong Database
        var exists = payments.findAll().stream()
                .anyMatch(p -> orderCodeStr.equals(p.getRequestId()));
        if (exists) {
          recordPayOS(data);
        }
      }
    } catch (Exception e) {
      // Bỏ qua lỗi dữ liệu thử nghiệm để luôn phản hồi 200 OK khi PayOS ping thử Webhook
      log.warn("Bỏ qua Webhook PayOS: {}", e.getMessage());
    }
  }

  private BusinessException error(String message) {
    return new BusinessException(HttpStatus.CONFLICT, "PAYMENT_CONFLICT", message);
  }
}