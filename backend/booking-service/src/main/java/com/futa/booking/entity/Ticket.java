// Vé điện tử của một hành khách và ghế
package com.futa.booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Entity
@Table(
    name = "tickets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id", "seat_code"}))
@Getter
@Setter
@NoArgsConstructor
public class Ticket {
  @Id private String code;

  @Column(nullable = false)
  private String bookingId;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String tripCode;

  @Column(nullable = false)
  private String seatCode;

  @Column(nullable = false, unique = true)
  private String qrToken;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false, precision = 15, scale = 0)
  private BigDecimal fare;

  @Column(nullable = false)
  private Instant issuedAt;
}
