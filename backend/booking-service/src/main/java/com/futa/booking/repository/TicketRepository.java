// Truy cập vé điện tử theo tài khoản hoặc đơn đặt
package com.futa.booking.repository;

import com.futa.booking.entity.Ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, String> {
  List<Ticket> findByTripCodeIn(java.util.Collection<String> tripCodes);

  List<Ticket> findByUserIdOrderByIssuedAtDesc(String userId);

  List<Ticket> findByBookingId(String bookingId);

  List<Ticket> findByTripCode(String tripCode);
}
