// Lịch gửi email vé sau giao dịch thanh toán
package com.futa.booking.scheduler;

import com.futa.booking.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingJobs {
  private final MailService mail;
  private final com.futa.booking.service.BookingService bookings;
  private final com.futa.booking.repository.BookingRepository repository;
  private final com.futa.booking.repository.RefundRepository refunds;
  private final com.futa.booking.service.RefundService refundService;
  private final com.futa.booking.repository.PaymentRepository payments;
  private final com.futa.booking.service.PaymentService paymentService;

  @Scheduled(fixedDelay = 30000)
  public void sendTickets() {
    mail.deliverPending();
  }

  // Nhả ghế của các đơn giữ chỗ quá hạn
  @Scheduled(fixedDelay = 30000)
  public void expire() {
    for (var booking : repository.findByStatusAndExpiresAtBefore("HELD", java.time.Instant.now()))
      bookings.expire(booking.getId());
  }

  // Đối soát các yêu cầu hoàn tiền chưa kết thúc
  @Scheduled(fixedDelay = 30000)
  public void refunds() {
    for (var refund :
        refunds.findTop10ByStatusAndNextAttemptAtBeforeOrderByRequestedAt(
            "PENDING", java.time.Instant.now())) refundService.process(refund.getTicketCode());
  }

  // Tra cứu giao dịch chưa nhận được thông báo từ MoMo
  @Scheduled(fixedDelay = 60000)
  public void payments() {
    for (var payment : payments.findByStatusOrderById("PENDING")) {
      try {
        paymentService.reconcile(payment.getId());
      } catch (com.futa.booking.exception.BusinessException unavailable) {
      }
    }
  }
}
