// Phiên thanh toán và kết quả đối soát từ MoMo
package com.futa.booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {
  @Id private String id;

  @Column(nullable = false, unique = true)
  private String bookingId;

  @Column(nullable = false)
  private String requestId;

  @Column(nullable = false, precision = 15, scale = 0)
  private BigDecimal amount;

  @Column(nullable = false)
  private String status;

  @Column(unique = true)
  private String providerTxnId;

  @Column(length = 4000)
  private String payUrl;

  @Column(length = 4000)
  private String qrCodeUrl;

  private Instant completedAt;
}
