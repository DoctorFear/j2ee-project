// Đồng bộ thao tác giữ và bán ghế theo từng chuyến
package com.futa.booking.service;

import com.futa.booking.repository.*;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {
  private final JdbcTemplate jdbc;
  private final TripInventoryRepository inventories;
  private final SeatReservationRepository seats;

  // Khóa cùng một tài nguyên ở mọi thao tác giữ, nhả và thanh toán
  public void lock(String code) {
    jdbc.update("INSERT IGNORE INTO trip_inventory(trip_code) VALUES (?)", code);
    inventories.lock(code).orElseThrow();
    seats.deleteByTripCodeAndStatusAndExpiresAtBefore(code, "HELD", Instant.now());
    seats.flush();
  }
}
