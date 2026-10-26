// Tra cứu email cần gửi và các lần gửi lại
package com.futa.booking.repository;

import com.futa.booking.entity.MailOutbox;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailOutboxRepository extends JpaRepository<MailOutbox, String> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  List<MailOutbox> findTop20BySentAtIsNullAndNextAttemptAtBeforeOrderByNextAttemptAt(Instant time);
}
