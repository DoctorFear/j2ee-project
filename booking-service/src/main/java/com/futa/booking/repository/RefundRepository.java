// Tra cứu yêu cầu hoàn tiền cần xử lý
package com.futa.booking.repository;

import com.futa.booking.entity.Refund;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, String> {
  List<Refund> findTop10ByStatusAndNextAttemptAtBeforeOrderByRequestedAt(
      String status, Instant time);
}
