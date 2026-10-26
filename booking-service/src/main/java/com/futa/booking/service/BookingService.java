// Giữ ghế, kiểm tra chủ sở hữu và giải phóng đơn hết hạn
package com.futa.booking.service;

import com.futa.booking.dto.*;
import com.futa.booking.entity.*;
import com.futa.booking.exception.BusinessException;
import com.futa.booking.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class BookingService {
  private final BookingRepository bookings;
  private final SeatReservationRepository seats;
  private final InventoryService inventory;
  private final CatalogService catalog;
  private final ObjectMapper mapper;
  private final jakarta.persistence.EntityManager entityManager;

  // Giữ tất cả ghế trong một giao dịch hoặc từ chối toàn bộ đơn
  @Transactional
  public Booking hold(HoldRequest request, String userId) {
    var trip = catalog.get(request.tripCode());
    inventory.lock(trip.code());
    var origin =
        trip.stops().stream()
            .filter(s -> s.stationId().equals(request.originId()))
            .findFirst()
            .orElseThrow(() -> error("INVALID_SEGMENT", "Điểm đón không hợp lệ."));
    var destination =
        trip.stops().stream()
            .filter(s -> s.stationId().equals(request.destinationId()))
            .findFirst()
            .orElseThrow(() -> error("INVALID_SEGMENT", "Điểm trả không hợp lệ."));
    Instant pickup = trip.departureAt().plusSeconds(origin.offsetMinutes() * 60L);
    if (!trip.bookable() || !pickup.isAfter(Instant.now()))
      throw error("TRIP_NOT_ON_SALE", "Chuyến chưa đủ điều kiện mở bán hoặc đã khởi hành.");
    if (!origin.active()
        || !destination.active()
        || !origin.allowPickup()
        || !destination.allowDropOff()
        || origin.order() >= destination.order())
      throw error(
          "INVALID_SEGMENT", "Điểm đón trả không đúng chiều hoặc không phục vụ hành khách.");
    var codes = new HashSet<>(request.seatCodes());
    if (codes.size() != request.seatCodes().size()
        || trip.vehicle().seats().stream().filter(s -> codes.contains(s.code())).count()
            != codes.size()) throw error("INVALID_SEATS", "Danh sách ghế không hợp lệ.");
    if (seats.findByTripCode(trip.code()).stream().anyMatch(s -> codes.contains(s.getSeatCode())))
      throw error("SEAT_UNAVAILABLE", "Ghế vừa được người khác giữ. Vui lòng chọn ghế khác.");
    if (trip.ticketPrice().compareTo(BigDecimal.valueOf(1000)) < 0)
      throw error("INVALID_PRICE", "Giá chuyến chưa phù hợp thanh toán MoMo.");
    var booking = new Booking();
    booking.setId(UUID.randomUUID().toString());
    booking.setUserId(userId);
    booking.setTripCode(trip.code());
    booking.setOriginId(request.originId());
    booking.setDestinationId(request.destinationId());
    booking.setContactName(request.name().trim());
    booking.setContactPhone(request.phone());
    booking.setContactEmail(request.email().trim());
    booking.setFare(trip.ticketPrice());
    booking.setAmount(trip.ticketPrice().multiply(BigDecimal.valueOf(codes.size())));
    booking.setStatus("HELD");
    booking.setCreatedAt(Instant.now());
    booking.setExpiresAt(Instant.now().plusSeconds(600));
    if (booking.getExpiresAt().isAfter(pickup)) booking.setExpiresAt(pickup);
    booking.setPickupAt(pickup);
    booking.setArrivalAt(trip.departureAt().plusSeconds(destination.offsetMinutes() * 60L));
    booking.setTripSnapshot(mapper.writeValueAsString(trip));
    booking.setSeatCodes(String.join(",", request.seatCodes()));
    bookings.saveAndFlush(booking);
    for (String code : codes) {
      var seat = new SeatReservation();
      seat.setTripCode(trip.code());
      seat.setSeatCode(code);
      seat.setBookingId(booking.getId());
      seat.setStatus("HELD");
      seat.setExpiresAt(booking.getExpiresAt());
      seats.save(seat);
    }
    seats.flush();
    return booking;
  }

  @Transactional(readOnly = true)
  public Booking owned(String id, String userId) {
    return bookings
        .findById(id)
        .filter(b -> b.getUserId().equals(userId))
        .orElseThrow(
            () ->
                new BusinessException(
                    HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", "Không tìm thấy đơn đặt vé."));
  }

  // Chỉ nhả ghế chưa thanh toán của chính người đặt
  @Transactional
  public void release(String id, String userId) {
    var before = owned(id, userId);
    inventory.lock(before.getTripCode());
    var booking = bookings.lock(id).orElseThrow();
    entityManager.refresh(booking, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (booking.getStatus().equals("HELD")) {
      booking.setStatus("CANCELLED");
      seats.deleteByBookingIdAndStatus(id, "HELD");
    }
  }

  // Cập nhật trạng thái đơn và nhả ghế theo thời gian máy chủ
  @Transactional
  public void expire(String id) {
    var before = bookings.findById(id).orElseThrow();
    inventory.lock(before.getTripCode());
    var booking = bookings.lock(id).orElseThrow();
    entityManager.refresh(booking, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    if (booking.getStatus().equals("HELD") && !booking.getExpiresAt().isAfter(Instant.now())) {
      booking.setStatus("EXPIRED");
      seats.deleteByBookingIdAndStatus(id, "HELD");
    }
  }

  private BusinessException error(String code, String message) {
    return new BusinessException(HttpStatus.CONFLICT, code, message);
  }
}
