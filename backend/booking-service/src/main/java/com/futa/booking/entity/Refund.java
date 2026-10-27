// Yêu cầu hoàn tiền vé và trạng thái xử lý với MoMo
package com.futa.booking.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
public class Refund {
  @Id private String ticketCode;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private Instant requestedAt;

  @Column(nullable = false)
  private Instant nextAttemptAt;

  @Column(nullable = false)
  private int attempts;

  private String providerTxnId;
}
