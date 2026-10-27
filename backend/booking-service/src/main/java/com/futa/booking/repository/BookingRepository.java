// Truy cập đơn đặt vé theo chủ sở hữu
package com.futa.booking.repository;

import com.futa.booking.entity.Booking;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, String> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Booking b where b.id=:id")
  Optional<Booking> lock(@Param("id") String id);

  List<Booking> findByStatusAndExpiresAtBefore(String status, java.time.Instant now);
}
