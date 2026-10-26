// Phát hành và tra cứu vé thuộc tài khoản khách hàng
package com.futa.booking.service;

import com.futa.booking.dto.*;
import com.futa.booking.entity.*;
import com.futa.booking.exception.BusinessException;
import com.futa.booking.repository.*;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {
  private final TicketRepository tickets;
  private final MailOutboxRepository outbox;
  private final BookingRepository bookings;
  private final CatalogService catalog;
  private final tools.jackson.databind.ObjectMapper mapper;

  // Ghép vé đã mua với trạng thái chuyến và giữ dữ liệu vé khi Transport gián đoạn
  @Transactional(readOnly = true)
  public List<TicketView> views(String userId) {
    var latest = new HashMap<String, String>();
    try {
      for (var trip : catalog.all()) latest.put(trip.code(), trip.status());
    } catch (BusinessException unavailable) {
    }
    return mine(userId).stream()
        .map(ticket -> view(ticket, latest.get(ticket.getTripCode())))
        .toList();
  }

  // Trả thông tin vé của đúng chủ sở hữu
  @Transactional(readOnly = true)
  public TicketView detail(String code, String userId) {
    var ticket = owned(code, userId);
    String status = null;
    try {
      status = catalog.get(ticket.getTripCode()).status();
    } catch (BusinessException unavailable) {
    }
    return view(ticket, status);
  }

  // Đọc hành trình và giá vé đã chốt khi khách đặt chỗ
  private TicketView view(Ticket ticket, String tripStatus) {
    var booking = bookings.findById(ticket.getBookingId()).orElseThrow();
    var trip = mapper.readValue(booking.getTripSnapshot(), Journey.class);
    return new TicketView(
        ticket.getCode(),
        ticket.getBookingId(),
        ticket.getTripCode(),
        ticket.getSeatCode(),
        booking.getOriginId(),
        booking.getDestinationId(),
        new TicketView.Contact(
            booking.getContactName(), booking.getContactPhone(), booking.getContactEmail()),
        ticket.getFare(),
        java.math.BigDecimal.ZERO,
        "MOMO",
        "PAID".equals(ticket.getStatus()) && "COMPLETED".equals(tripStatus)
            ? "COMPLETED"
            : ticket.getStatus(),
        ticket.getQrToken(),
        trip,
        booking.getPickupAt(),
        booking.getArrivalAt());
  }

  // Chỉ cấp mã xuất trình cho vé còn hiệu lực lên xe
  public Map<String, String> qr(String code, String userId) {
    var ticket = detail(code, userId);
    if (!ticket.status().equals("PAID"))
      throw new BusinessException(
          HttpStatus.CONFLICT, "TICKET_NOT_VALID", "Vé không còn hiệu lực lên xe.");
    return Map.of("code", ticket.code(), "qrToken", ticket.qrToken());
  }

  // Mỗi ghế phát hành một vé trong cùng giao dịch xác nhận thanh toán
  public void issue(Booking booking) {
    if (!tickets.findByBookingId(booking.getId()).isEmpty()) return;
    for (String seat : booking.getSeatCodes().split(",")) {
      Ticket ticket = new Ticket();
      ticket.setCode(
          "FUTA-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
      ticket.setBookingId(booking.getId());
      ticket.setUserId(booking.getUserId());
      ticket.setTripCode(booking.getTripCode());
      ticket.setSeatCode(seat);
      ticket.setFare(booking.getFare());
      ticket.setQrToken(UUID.randomUUID().toString() + UUID.randomUUID());
      ticket.setStatus("PAID");
      ticket.setIssuedAt(Instant.now());
      tickets.save(ticket);
    }
    MailOutbox mail = new MailOutbox();
    mail.setBookingId(booking.getId());
    mail.setNextAttemptAt(Instant.now());
    outbox.save(mail);
  }

  @Transactional(readOnly = true)
  public List<Ticket> mine(String userId) {
    return tickets.findByUserIdOrderByIssuedAtDesc(userId);
  }

  @Transactional(readOnly = true)
  public Ticket owned(String code, String userId) {
    return tickets
        .findById(code)
        .filter(t -> t.getUserId().equals(userId))
        .orElseThrow(
            () ->
                new BusinessException(
                    HttpStatus.NOT_FOUND, "TICKET_NOT_FOUND", "Không tìm thấy vé của bạn."));
  }
}
