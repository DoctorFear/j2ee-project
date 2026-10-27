// Xử lý hủy vé trước giờ xuất bến năm tiếng và hoàn tiền
package com.futa.booking.service;

import com.futa.booking.client.MomoClient;
import com.futa.booking.entity.Refund;
import com.futa.booking.exception.BusinessException;
import com.futa.booking.repository.*;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {
  private final TicketService ticketService;
  private final TicketRepository tickets;
  private final RefundRepository refunds;
  private final PaymentRepository payments;
  private final SeatReservationRepository seats;
  private final InventoryService inventory;
  private final MomoClient momo;
  private final CatalogService catalog;
  private final jakarta.persistence.EntityManager entityManager;

  // Chốt thời điểm khách yêu cầu hủy theo giờ xuất bến của xe
  @Transactional
  public Refund request(String code, String userId) {
    var ticket = ticketService.owned(code, userId);
    inventory.lock(ticket.getTripCode());
    entityManager.refresh(ticket, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (refunds.existsById(code)) return refunds.findById(code).orElseThrow();
    var trip = catalog.get(ticket.getTripCode());
    if (!ticket.getStatus().equals("PAID")
        || !trip.status().equals("SCHEDULED")
        || Instant.now().isAfter(trip.departureAt().minusSeconds(5 * 3600)))
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "CANCELLATION_CLOSED",
          "Chỉ được hủy và hoàn vé khi còn ít nhất 5 tiếng trước giờ xe khởi hành.");
    // Vé khởi tạo không có giao dịch MoMo để yêu cầu hoàn tiền thực tế
    var originalPayment = payments.findByBookingId(ticket.getBookingId()).orElseThrow();
    if (originalPayment.getProviderTxnId() != null
        && originalPayment.getProviderTxnId().startsWith("SEED-"))
      throw new BusinessException(
          HttpStatus.CONFLICT,
          "SEED_PAYMENT",
          "Vé khởi tạo không có giao dịch MoMo thực tế để hoàn tiền. Vui lòng đặt và thanh toán vé"
              + " mới để kiểm tra hoàn tiền.");
    var refund = new Refund();
    refund.setTicketCode(code);
    refund.setStatus("PENDING");
    refund.setRequestedAt(Instant.now());
    refund.setNextAttemptAt(Instant.now());
    return refunds.save(refund);
  }

  @Transactional(readOnly = true)
  public Refund status(String code, String userId) {
    ticketService.owned(code, userId);
    return refunds.findById(code).orElse(null);
  }

  // Chỉ trả lại ghế khi cổng thanh toán xác nhận hoàn tiền thành công
  @Transactional
  public void process(String code) {
    var ticket = tickets.findById(code).orElseThrow();
    inventory.lock(ticket.getTripCode());
    var refund = refunds.findById(code).orElseThrow();
    entityManager.refresh(ticket, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    entityManager.refresh(refund, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (!refund.getStatus().equals("PENDING")) return;
    var payment = payments.findByBookingId(ticket.getBookingId()).orElseThrow();
    try {
      String id = "REF-" + code;
      var result =
          momo.refund(id, payment.getProviderTxnId(), ticket.getFare(), refund.getAttempts() > 0);
      var confirmation = result;
      if (refund.getAttempts() > 0) {
        confirmation = null;
        for (var item : result.path("refundTrans"))
          if (item.path("orderId").asText().equals(id)) confirmation = item;
      }
      if (confirmation != null
          && confirmation.path("resultCode").asInt(-1) == 0
          && confirmation.path("transId").asLong(0) > 0) {
        if (confirmation.has("amount")
            && confirmation.path("amount").decimalValue().compareTo(ticket.getFare()) != 0)
          throw new IllegalStateException();
        refund.setStatus("SUCCESS");
        refund.setProviderTxnId(confirmation.path("transId").asText());
        ticket.setStatus("CANCELLED");
        var reservation =
            seats.findByBookingId(ticket.getBookingId()).stream()
                .filter(s -> s.getSeatCode().equals(ticket.getSeatCode()))
                .toList();
        seats.deleteAll(reservation);
        return;
      }
    } catch (Exception ignored) {
    }
    refund.setAttempts(refund.getAttempts() + 1);
    refund.setNextAttemptAt(Instant.now().plusSeconds(60));
    if (refund.getAttempts() >= 10) refund.setStatus("REVIEW_REQUIRED");
  }
}
