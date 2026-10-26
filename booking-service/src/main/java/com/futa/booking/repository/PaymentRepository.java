// Lưu và khóa phiên thanh toán khi nhận callback
package com.futa.booking.repository;

import com.futa.booking.entity.Payment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, String> {
  Optional<Payment> findByBookingId(String bookingId);

  java.util.List<Payment> findByStatusOrderById(String status);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Payment p where p.id=:id")
  Optional<Payment> lock(@Param("id") String id);
}
