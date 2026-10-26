// Khởi tạo đơn và vé khách hàng từ dữ liệu vận tải trong môi trường local
package com.futa.booking.seed;

import com.futa.booking.client.TransportClient;
import com.futa.booking.config.BookingProperties;
import com.futa.booking.dto.Journey;
import com.futa.booking.entity.*;
import com.futa.booking.repository.*;
import com.futa.booking.service.CatalogService;
import com.futa.booking.service.InventoryService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

@Component
@Profile("local")
@ConditionalOnProperty(
    prefix = "booking.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder {
  private final TransportClient transport;
  private final CatalogService catalog;
  private final BookingProperties properties;
  private final BookingRepository bookings;
  private final TicketRepository tickets;
  private final PaymentRepository payments;
  private final SeatReservationRepository reservations;
  private final RefundRepository refunds;
  private final MailOutboxRepository outbox;
  private final TripInventoryRepository inventories;
  private final InventoryService inventory;
  private final PlatformTransactionManager transactionManager;
  private final ObjectMapper mapper;

  @Value("${booking.seed.user-id:}")
  private String userId;

  @Value("${booking.seed.name:Khách hàng Nguyễn Văn An}")
  private String name;

  @Value("${booking.seed.phone:0902345601}")
  private String phone;

  @Value("${booking.seed.email:member1@futabus.vn}")
  private String email;

  private boolean initialized;
  private boolean waitingLogged;

  // Chờ Transport sẵn sàng rồi đồng bộ đủ năm đơn trong một giao dịch
  @Scheduled(initialDelay = 2000, fixedDelay = 15000)
  public void seed() {
    if (initialized) return;
    if (!userId.matches("[1-9][0-9]*")) {
      if (!waitingLogged) {
        log.warn(
            "DatabaseSeeder: can cau hinh booking.seed.user-id trung ID tai khoan member1 trong"
                + " Auth.");
        waitingLogged = true;
      }
      return;
    }
    try {
      var rawTrips = transport.list("trips");
      String prefix = "SEED-";
      if (rawTrips.stream().noneMatch(t -> t.path("code").asText().equals(prefix + "01")))
        throw new IllegalStateException("Chua co bo chuyen seed cua Transport");
      var vehicleIds =
          rawTrips.stream()
              .filter(t -> t.path("code").asText().startsWith(prefix))
              .map(t -> t.path("vehicleId").asText())
              .collect(Collectors.toSet());
      configureLayouts(vehicleIds);
      var journeys =
          catalog.all().stream()
              .filter(j -> j.code().startsWith(prefix))
              .collect(Collectors.toMap(Journey::code, j -> j));
      for (String suffix : List.of("01", "09", "11", "12", "17"))
        if (!journeys.containsKey(prefix + suffix))
          throw new IllegalStateException("Transport chua tao du bo chuyen seed");
      new TransactionTemplate(transactionManager)
          .executeWithoutResult(
              status -> {
                clearSeedBookings();
                inventory.lock(prefix + "BOOKINGS");
                create(
                    prefix,
                    "01",
                    journeys.get(prefix + "01"),
                    List.of("A01", "B01"),
                    false,
                    0,
                    7);
                create(prefix, "02", journeys.get(prefix + "09"), List.of("A05"), false, 0, 4);
                create(
                    prefix,
                    "03",
                    journeys.get(prefix + "17"),
                    List.of("A10", "B10", "A11"),
                    false,
                    0,
                    7);
                create(
                    prefix,
                    "04",
                    journeys.get(prefix + "11"),
                    List.of("A16", "B16"),
                    true,
                    1,
                    3);
                create(
                    prefix,
                    "05",
                    journeys.get(prefix + "12"),
                    List.of("A01", "A02", "A03", "B01", "B02"),
                    false,
                    0,
                    2);
              });
      initialized = true;
      log.info(
          "DatabaseSeeder: san sang 5 booking / 13 ve cho userId={}, bo chuyen {}. Khong tao giao"
              + " dich MoMo hoac gui email.",
          userId,
          prefix);
    } catch (Exception error) {
      if (!waitingLogged) {
        log.warn(
            "DatabaseSeeder: dang cho du lieu Transport va cau hinh hop le; se thu lai. {}",
            error.getClass().getSimpleName());
        waitingLogged = true;
      }
    }
  }

  // Bổ sung sơ đồ đúng sức chứa cho các xe thuộc bộ chuyến seed
  private void configureLayouts(Set<String> ids) {
    for (var vehicle : transport.list("vehicles")) {
      String id = vehicle.path("id").asText();
      if (!ids.contains(id)
          || !vehicle.path("licensePlate").asText().matches("77B-900\\.(0[1-9]|1[0-9]|20)"))
        continue;
      int capacity = vehicle.path("capacity").asInt();
      if (capacity < 1 || capacity > 60)
        throw new IllegalStateException("Suc chua xe seed khong hop le");
      var codes = new ArrayList<String>();
      int lower = vehicle.path("type").asText().equals("SEAT") ? capacity : (capacity + 1) / 2;
      for (int i = 1; i <= lower; i++) codes.add(String.format("A%02d", i));
      for (int i = 1; i <= capacity - lower; i++) codes.add(String.format("B%02d", i));
      properties.getVehicleSeats().putIfAbsent(id, List.copyOf(codes));
    }
  }

  // Xóa riêng dữ liệu seed cũ để lần chạy sau luôn có đúng một bộ dữ liệu
  private void clearSeedBookings() {
    for (Booking booking : bookings.findAll()) {
      if (!booking.getId().matches("SEED(-[0-9]{8})?-BOOKING-[0-9]{2}")) continue;
      for (Ticket ticket : tickets.findByBookingId(booking.getId())) {
        refunds.deleteById(ticket.getCode());
        tickets.delete(ticket);
      }
      reservations.deleteAll(reservations.findByBookingId(booking.getId()));
      payments.findByBookingId(booking.getId()).ifPresent(payments::delete);
      outbox.deleteById(booking.getId());
      bookings.delete(booking);
    }
    inventories.deleteAll(
        inventories.findAll().stream()
            .filter(inventory -> inventory.getTripCode().startsWith("SEED-"))
            .toList());
    bookings.flush();
  }

  // Lưu đơn, thanh toán khởi tạo, vé và ghế đồng bộ
  private void create(
      String prefix,
      String suffix,
      Journey trip,
      List<String> codes,
      boolean cancelled,
      int originIndex,
      int destinationIndex) {
    String id = prefix + "BOOKING-" + suffix;
    if (bookings.existsById(id)) return;
    if (codes.stream()
        .anyMatch(code -> trip.vehicle().seats().stream().noneMatch(s -> s.code().equals(code))))
      throw new IllegalStateException("Ma ghe seed khong khop so do xe");
    inventory.lock(trip.code());
    if (reservations.findByTripCode(trip.code()).stream()
        .anyMatch(s -> codes.contains(s.getSeatCode())))
      throw new IllegalStateException("Ghe seed da duoc su dung");
    var origin = trip.stops().get(originIndex);
    var destination = trip.stops().get(destinationIndex);
    Instant created = Instant.now().minusSeconds(3600);
    Instant beforeTrip = trip.departureAt().minusSeconds(86400);
    if (created.isAfter(beforeTrip)) created = beforeTrip;
    Booking order = new Booking();
    order.setId(id);
    order.setUserId(userId);
    order.setTripCode(trip.code());
    order.setOriginId(origin.stationId());
    order.setDestinationId(destination.stationId());
    order.setContactName(name);
    order.setContactPhone(phone);
    order.setContactEmail(email);
    order.setFare(trip.ticketPrice());
    order.setAmount(trip.ticketPrice().multiply(java.math.BigDecimal.valueOf(codes.size())));
    order.setStatus("PAID");
    order.setCreatedAt(created);
    order.setExpiresAt(created.plusSeconds(600));
    order.setPickupAt(trip.departureAt().plusSeconds(origin.offsetMinutes() * 60L));
    order.setArrivalAt(trip.departureAt().plusSeconds(destination.offsetMinutes() * 60L));
    order.setTripSnapshot(mapper.writeValueAsString(trip));
    order.setSeatCodes(String.join(",", codes));
    bookings.saveAndFlush(order);
    Payment payment = new Payment();
    payment.setId("SEED-PAY-" + id);
    payment.setBookingId(id);
    payment.setRequestId("SEED-REQ-" + id);
    payment.setAmount(order.getAmount());
    payment.setStatus("SUCCESS");
    payment.setProviderTxnId("SEED-TXN-" + id);
    payment.setCompletedAt(created.plusSeconds(60));
    payments.save(payment);
    for (String code : codes) {
      Ticket ticket = new Ticket();
      ticket.setCode(prefix + "T" + suffix + "-" + code);
      ticket.setBookingId(id);
      ticket.setUserId(userId);
      ticket.setTripCode(trip.code());
      ticket.setSeatCode(code);
      ticket.setQrToken(
          UUID.nameUUIDFromBytes((id + code).getBytes(StandardCharsets.UTF_8)).toString());
      ticket.setStatus(cancelled ? "CANCELLED" : "PAID");
      ticket.setFare(trip.ticketPrice());
      ticket.setIssuedAt(payment.getCompletedAt());
      tickets.saveAndFlush(ticket);
      if (cancelled) {
        Refund refund = new Refund();
        refund.setTicketCode(ticket.getCode());
        refund.setStatus("SUCCESS");
        refund.setRequestedAt(created.plusSeconds(180));
        refund.setNextAttemptAt(refund.getRequestedAt());
        refund.setProviderTxnId("SEED-REFUND-" + ticket.getCode());
        refunds.save(refund);
      } else {
        SeatReservation seat = new SeatReservation();
        seat.setTripCode(trip.code());
        seat.setSeatCode(code);
        seat.setBookingId(id);
        seat.setStatus("SOLD");
        seat.setExpiresAt(order.getExpiresAt());
        reservations.save(seat);
      }
    }
    reservations.flush();
  }
}
