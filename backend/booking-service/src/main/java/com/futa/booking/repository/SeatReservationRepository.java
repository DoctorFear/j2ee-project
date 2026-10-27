// Tra cứu và giải phóng ghế theo chuyến hoặc đơn
package com.futa.booking.repository;

import com.futa.booking.entity.SeatReservation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
  List<SeatReservation> findByTripCode(String tripCode);

  List<SeatReservation> findByBookingId(String bookingId);

  void deleteByBookingIdAndStatus(String bookingId, String status);

  void deleteByTripCodeAndStatusAndExpiresAtBefore(
      String tripCode, String status, java.time.Instant time);
}
