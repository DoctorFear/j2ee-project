// Hàng đợi email vé có khả năng gửi lại khi thất bại
package com.futa.booking.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "mail_outbox")
@Getter
@Setter
@NoArgsConstructor
public class MailOutbox {
  @Id private String bookingId;

  @Column(nullable = false)
  private int attempts;

  @Column(nullable = false)
  private Instant nextAttemptAt;

  private Instant sentAt;
}
