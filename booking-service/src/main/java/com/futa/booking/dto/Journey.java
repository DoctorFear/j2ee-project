// Thông tin chuyến và cấu hình ghế dùng cho giao diện đặt vé
package com.futa.booking.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Journey(
    String code,
    String routeName,
    Instant departureAt,
    Instant arrivalAt,
    String status,
    String saleStatus,
    BigDecimal ticketPrice,
    List<Stop> stops,
    List<Station> stations,
    Vehicle vehicle,
    List<String> soldSeats,
    List<String> heldSeats,
    boolean bookable) {
  public record Stop(
      String stationId,
      int order,
      BigDecimal distanceKm,
      int offsetMinutes,
      boolean active,
      boolean allowPickup,
      boolean allowDropOff) {}

  public record Station(String id, String name, String province, String address) {}

  public record Seat(String code, String deck, int row, String position) {}

  public record Vehicle(String id, String name, List<Seat> seats) {}
}
