// Ghép dữ liệu vận tải và tình trạng ghế cho khách hàng
package com.futa.booking.service;

import com.futa.booking.client.TransportClient;
import com.futa.booking.config.BookingProperties;
import com.futa.booking.dto.Journey;
import com.futa.booking.exception.BusinessException;
import com.futa.booking.repository.SeatReservationRepository;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class CatalogService {
  private final TransportClient transport;
  private final SeatReservationRepository reservations;
  private final BookingProperties properties;

  // Đọc các danh mục hiện có và giữ nguyên giá chuyến của Transport
  public List<Journey> all() {
    var stationMap = index(transport.list("stations"));
    var routes = index(transport.list("routes"));
    var vehicles = index(transport.list("vehicles"));
    var allStops = transport.list("route-stops");
    var result = new ArrayList<Journey>();
    for (var trip : transport.list("trips")) {
      var route = routes.get(trip.path("routeId").asText());
      var vehicle = vehicles.get(trip.path("vehicleId").asText());
      if (route == null || vehicle == null) continue;
      var stops =
          allStops.stream()
              .filter(s -> s.path("routeId").asText().equals(trip.path("routeId").asText()))
              .sorted(Comparator.comparingInt(s -> s.path("stopOrder").asInt()))
              .map(
                  s -> {
                    var physical = stationMap.get(s.path("stationId").asText());
                    return new Journey.Stop(
                        s.path("stationId").asText(),
                        s.path("stopOrder").asInt(),
                        s.path("distanceKm").decimalValue(),
                        s.path("offsetMinutes").asInt(),
                        physical != null && physical.path("status").asText().equals("ACTIVE"),
                        s.path("allowPickup").asBoolean(),
                        s.path("allowDropOff").asBoolean());
                  })
              .toList();
      if (stops.size() < 2) continue;
      var stations =
          stops.stream()
              .map(s -> stationMap.get(s.stationId()))
              .filter(Objects::nonNull)
              .map(
                  s ->
                      new Journey.Station(
                          s.path("id").asText(),
                          s.path("name").asText(),
                          "Điểm phục vụ",
                          s.path("address").asText()))
              .toList();
      var seatCodes =
          properties.getVehicleSeats().getOrDefault(vehicle.path("id").asText(), List.of());
      boolean layoutValid =
          seatCodes.size() == trip.path("capacitySnapshot").asInt()
              && seatCodes.stream().distinct().count() == seatCodes.size()
              && !seatCodes.isEmpty()
              && seatCodes.size() == vehicle.path("capacity").asInt()
              && seatCodes.stream().allMatch(s -> s != null && s.matches("[AB][0-9]{2,3}"));
      var seats = new ArrayList<Journey.Seat>();
      if (layoutValid)
        for (String code : seatCodes) {
          var deck = seatCodes.stream().filter(s -> s.charAt(0) == code.charAt(0)).toList();
          int position = deck.indexOf(code);
          seats.add(
              new Journey.Seat(
                  code,
                  code.substring(0, 1),
                  position / 3 + 1,
                  position < 3 ? "front" : position >= deck.size() - 3 ? "rear" : "middle"));
        }
      String type =
          switch (vehicle.path("type").asText()) {
            case "SEAT" -> "Ghế ngồi";
            case "BERTH" -> "Giường nằm";
            case "LIMOUSINE" -> "Limousine";
            default -> "Xe";
          };
      var layout =
          new Journey.Vehicle(
              vehicle.path("id").asText(),
              type + " " + trip.path("capacitySnapshot").asText() + " chỗ",
              seats);
      var held = reservations.findByTripCode(trip.path("code").asText());
      // Ngừng mở bán khi sơ đồ xe hoặc mốc lộ trình không còn phù hợp
      boolean timelineValid = true;
      for (int i = 0; i < stops.size(); i++) {
        var stop = stops.get(i);
        if (stop.offsetMinutes() < 0 || stop.distanceKm().signum() < 0) timelineValid = false;
        if (i > 0
            && (stop.order() <= stops.get(i - 1).order()
                || stop.offsetMinutes() <= stops.get(i - 1).offsetMinutes()
                || stop.distanceKm().compareTo(stops.get(i - 1).distanceKm()) < 0))
          timelineValid = false;
      }
      var departure = instant(trip.path("departureAt").asText());
      var arrival = instant(trip.path("arrivalAt").asText());
      boolean effective =
          (route.path("effectiveFromAt").asText("").isBlank()
                  || !departure.isBefore(instant(route.path("effectiveFromAt").asText())))
              && (route.path("effectiveToAt").asText("").isBlank()
                  || departure.isBefore(instant(route.path("effectiveToAt").asText())));
      boolean bookable =
          properties.isSalesEnabled()
              && layoutValid
              && timelineValid
              && effective
              && arrival.isAfter(departure)
              && held.stream()
                  .filter(
                      s -> s.getStatus().equals("SOLD") || s.getExpiresAt().isAfter(Instant.now()))
                  .allMatch(s -> seatCodes.contains(s.getSeatCode()))
              && vehicle.path("status").asText().equals("ACTIVE")
              && route.path("status").asText().equals("ACTIVE")
              && trip.path("status").asText().equals("SCHEDULED")
              && trip.path("saleStatus").asText().equals("OPEN");
      result.add(
          new Journey(
              trip.path("code").asText(),
              route.path("name").asText(),
              departure,
              arrival,
              trip.path("status").asText(),
              trip.path("saleStatus").asText(),
              trip.path("ticketPrice").decimalValue(),
              stops,
              stations,
              layout,
              held.stream()
                  .filter(s -> s.getStatus().equals("SOLD"))
                  .map(s -> s.getSeatCode())
                  .toList(),
              held.stream()
                  .filter(
                      s -> s.getStatus().equals("HELD") && s.getExpiresAt().isAfter(Instant.now()))
                  .map(s -> s.getSeatCode())
                  .toList(),
              bookable));
    }
    return result;
  }

  // Tra cứu chuyến theo mã dùng chung giữa các service
  public Journey get(String code) {
    return all().stream()
        .filter(t -> t.code().equals(code))
        .findFirst()
        .orElseThrow(
            () ->
                new BusinessException(
                    HttpStatus.NOT_FOUND, "TRIP_NOT_FOUND", "Không tìm thấy chuyến xe."));
  }

  // Chỉ công bố các trạm đang phục vụ hành khách
  public List<Journey.Station> stations() {
    return transport.list("stations").stream()
        .filter(s -> s.path("status").asText().equals("ACTIVE"))
        .map(
            s ->
                new Journey.Station(
                    s.path("id").asText(),
                    s.path("name").asText(),
                    "Điểm phục vụ",
                    s.path("address").asText()))
        .toList();
  }

  private Map<String, JsonNode> index(List<JsonNode> values) {
    return values.stream()
        .collect(Collectors.toMap(s -> s.path("id").asText(), Function.identity()));
  }

  private Instant instant(String date) {
    try {
      return Instant.parse(date);
    } catch (java.time.format.DateTimeParseException e) {
      return LocalDateTime.parse(date).atZone(ZoneId.of(properties.getTransportZone())).toInstant();
    }
  }
}
