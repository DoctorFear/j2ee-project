// Cung cấp số vé, doanh thu và số khách tại trạm cho dịch vụ vận tải
package com.futa.booking.service;

import com.futa.booking.dto.*;
import com.futa.booking.entity.Ticket;
import com.futa.booking.repository.*;
import java.math.BigDecimal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingIntegrationService {
  private final TicketRepository tickets;
  private final BookingRepository bookings;

  // Tổng hợp tiền đã thu và tiền đã hoàn riêng cho từng chuyến
  public List<TripSales> sales(List<String> codes) {
    var values = tickets.findByTripCodeIn(codes);
    return codes.stream()
        .distinct()
        .map(
            code -> {
              var items = values.stream().filter(t -> t.getTripCode().equals(code)).toList();
              BigDecimal gross =
                  items.stream().map(Ticket::getFare).reduce(BigDecimal.ZERO, BigDecimal::add);
              var cancelled =
                  items.stream().filter(t -> t.getStatus().equals("CANCELLED")).toList();
              BigDecimal refunded =
                  cancelled.stream().map(Ticket::getFare).reduce(BigDecimal.ZERO, BigDecimal::add);
              return new TripSales(
                  code,
                  items.size() - cancelled.size(),
                  cancelled.size(),
                  gross,
                  refunded,
                  gross.subtract(refunded));
            })
        .toList();
  }

  // Đếm khách có vé hợp lệ tại điểm đón và điểm trả đã đặt
  public List<StopCount> stops(String code) {
    var counts = new LinkedHashMap<String, long[]>();
    var tripTickets =
        tickets.findByTripCode(code).stream()
            .filter(t -> !t.getStatus().equals("CANCELLED"))
            .toList();
    var orders = new HashMap<String, com.futa.booking.entity.Booking>();
    bookings
        .findAllById(tripTickets.stream().map(Ticket::getBookingId).distinct().toList())
        .forEach(b -> orders.put(b.getId(), b));
    for (var ticket : tripTickets) {
      var order = orders.get(ticket.getBookingId());
      counts.computeIfAbsent(order.getOriginId(), k -> new long[2])[0]++;
      counts.computeIfAbsent(order.getDestinationId(), k -> new long[2])[1]++;
    }
    return counts.entrySet().stream()
        .map(e -> new StopCount(e.getKey(), e.getValue()[0], e.getValue()[1]))
        .toList();
  }
}
