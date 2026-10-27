// Ghế đang giữ hoặc đã bán trên một chuyến
package com.futa.booking.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(
    name = "seat_reservations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"trip_code", "seat_code"}))
@Getter
@Setter
@NoArgsConstructor
public class SeatReservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String tripCode;

  @Column(nullable = false)
  private String seatCode;

  @Column(nullable = false)
  private String bookingId;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private Instant expiresAt;
}
