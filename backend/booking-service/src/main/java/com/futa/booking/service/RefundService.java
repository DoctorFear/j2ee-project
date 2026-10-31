// Xử lý hủy vé trước giờ xuất bến năm tiếng và hoàn tiền
package com.futa.booking.service;

import com.futa.booking.client.PayOSClient;
import com.futa.booking.entity.Refund;
import com.futa.booking.exception.BusinessException;
import com.futa.booking.repository.*;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {
  private final TicketService ticketService;
  private final TicketRepository tickets;
  private final RefundRepository refunds;
  private final PaymentRepository payments;
  private final SeatReservationRepository seats;
  private final InventoryService inventory;
  // Thay thế MomoClient bằng PayOSClient
  private final PayOSClient payOSClient;
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
    // Vé khởi tạo không có giao dịch PayOS thực tế để yêu cầu hoàn tiền
    var originalPayment = payments.findByBookingId(ticket.getBookingId()).orElseThrow();
    if (originalPayment.getProviderTxnId() != null
            && originalPayment.getProviderTxnId().startsWith("SEED-"))
      throw new BusinessException(
              HttpStatus.CONFLICT,
              "SEED_PAYMENT",
              "Vé khởi tạo không có giao dịch PayOS thực tế để hoàn tiền. Vui lòng đặt và thanh toán vé"
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

  // Chỉ trả lại ghế khi cổng thanh toán xác nhận hoàn tiền / hủy liên kết thành công
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
      // Lấy orderCode dạng số đã dùng khi tạo link thanh toán PayOS (lưu trong requestId)
      long orderCode = Long.parseLong(payment.getRequestId());

      // Gọi hủy link thanh toán hoặc xác nhận hủy qua PayOS
      boolean cancelled = payOSClient.cancelPaymentLink(orderCode, "Khach hang yeu cau huy ve: " + code);
      if (cancelled) {
        refund.setStatus("SUCCESS");
        refund.setProviderTxnId("PAYOS-REF-" + code);
        ticket.setStatus("CANCELLED");
        var reservation =
                seats.findByBookingId(ticket.getBookingId()).stream()
                        .filter(s -> s.getSeatCode().equals(ticket.getSeatCode()))
                        .toList();
        seats.deleteAll(reservation);
        return;
      }
    } catch (Exception e) {
      log.warn("Xử lý hoàn tiền thất bại, ticket={}: {}", code, e.getMessage());
    }
    refund.setAttempts(refund.getAttempts() + 1);
    refund.setNextAttemptAt(Instant.now().plusSeconds(60));
    if (refund.getAttempts() >= 10) refund.setStatus("REVIEW_REQUIRED");
  }
}