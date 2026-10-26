// Đơn đặt vé và bản chụp hành trình tại thời điểm giữ chỗ
package com.futa.booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class Booking {
  @Id private String id;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String tripCode;

  @Column(nullable = false)
  private String originId;

  @Column(nullable = false)
  private String destinationId;

  @Column(nullable = false)
  private String contactName;

  @Column(nullable = false)
  private String contactPhone;

  @Column(nullable = false)
  private String contactEmail;

  @Column(nullable = false, precision = 15, scale = 0)
  private BigDecimal fare;

  @Column(nullable = false, precision = 15, scale = 0)
  private BigDecimal amount;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant pickupAt;

  @Column(nullable = false)
  private Instant arrivalAt;

  @Column(nullable = false, columnDefinition = "LONGTEXT")
  private String tripSnapshot;

  @Column(nullable = false)
  private String seatCodes;
}
