// Khởi tạo tuyến, phương tiện và chuyến xe phục vụ môi trường local
package com.futabus.transport.seed;

import com.futabus.transport.resource.entity.*;
import com.futabus.transport.resource.enums.*;
import com.futabus.transport.resource.repository.*;
import com.futabus.transport.route.entity.*;
import com.futabus.transport.route.enums.*;
import com.futabus.transport.route.repository.*;
import com.futabus.transport.trip.entity.*;
import com.futabus.transport.trip.enums.*;
import com.futabus.transport.trip.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("local")
@ConditionalOnProperty(prefix = "futa.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DatabaseSeeder implements ApplicationRunner {
    StationRepository stationRepository;
    StationAddressRepository stationAddressRepository;
    RouteRepository routeRepository;
    RouteStopRepository routeStopRepository;
    VehicleRepository vehicleRepository;
    DriverRepository driverRepository;
    TripRepository tripRepository;
    TripDriverRepository tripDriverRepository;
    JdbcTemplate jdbcTemplate;
    Clock transportClock;

    // Tạo dữ liệu theo lịch của lần khởi tạo đầu tiên
    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        jdbcTemplate.queryForList("SELECT pg_advisory_xact_lock(19890531)");
        LocalDateTime now = LocalDateTime.now(transportClock).withSecond(0).withNano(0);
        LocalDate today = now.toLocalDate();
        String prefix = "SEED-";

        Station mienTay = station("Bến xe Miền Tây (TP. Hồ Chí Minh)", "395 Kinh Dương Vương, TP. Hồ Chí Minh",
                StationType.TERMINAL, StationStatus.ACTIVE);
        Station anSuong = station("Bến xe An Sương (TP. Hồ Chí Minh)", "Quốc lộ 22, TP. Hồ Chí Minh",
                StationType.TERMINAL, StationStatus.ACTIVE);
        Station phanThiet = station("Điểm đón Phan Thiết", "Quốc lộ 1A, Phan Thiết", StationType.ROAD_STOP,
                StationStatus.ACTIVE);
        Station nhaTrang = station("Bến xe phía Nam Nha Trang", "Đường 23/10, Nha Trang, Khánh Hòa",
                StationType.TERMINAL, StationStatus.ACTIVE);
        Station tuyHoa = station("Bến xe Nam Tuy Hòa", "Quốc lộ 1A, Tuy Hòa, Phú Yên", StationType.TERMINAL,
                StationStatus.ACTIVE);
        Station quyNhon = station("Văn phòng Quy Nhơn", "207 Tây Sơn, Quy Nhơn, Bình Định", StationType.OFFICE,
                StationStatus.ACTIVE);
        Station daLat = station("Bến xe liên tỉnh Đà Lạt", "01 Tô Hiến Thành, Đà Lạt, Lâm Đồng", StationType.TERMINAL,
                StationStatus.ACTIVE);
        Station daNang = station("Bến xe trung tâm Đà Nẵng", "185 Tôn Đức Thắng, Đà Nẵng", StationType.TERMINAL,
                StationStatus.ACTIVE);
        Station inactive = station("Văn phòng Phú Tài (tạm ngưng)", "Quốc lộ 1A, khu vực Phú Tài, Quy Nhơn",
                StationType.OFFICE, StationStatus.INACTIVE);
        Station anNhon = station("Bến xe An Nhơn (Bình Định)", "Quốc lộ 1A, An Nhơn, Bình Định", StationType.TERMINAL,
                StationStatus.ACTIVE);

        var mainStations = List.of(mienTay, anSuong, phanThiet, nhaTrang, tuyHoa, inactive, quyNhon, anNhon);
        int[] km = { 0, 20, 200, 430, 540, 630, 650, 670 };
        int[] minutes = { 0, 45, 240, 480, 600, 735, 780, 825 };
        Route outbound = route("SEED-HCM-BD", "TP. Hồ Chí Minh → Bình Định", 350000, RouteStatus.ACTIVE, mainStations,
                km, minutes);
        var reverse = new ArrayList<>(mainStations);
        Collections.reverse(reverse);
        int[] reverseKm = new int[km.length], reverseMinutes = new int[minutes.length];
        for (int i = 0; i < km.length; i++) {
            reverseKm[i] = km[km.length - 1] - km[km.length - 1 - i];
            reverseMinutes[i] = minutes[minutes.length - 1] - minutes[minutes.length - 1 - i];
        }
        Route inbound = route("SEED-BD-HCM", "Bình Định → TP. Hồ Chí Minh", 350000, RouteStatus.ACTIVE, reverse,
                reverseKm, reverseMinutes);
        Route nt = route("SEED-HCM-NT", "TP. Hồ Chí Minh → Nha Trang", 280000, RouteStatus.ACTIVE,
                List.of(mienTay, anSuong, phanThiet, nhaTrang), new int[] { 0, 20, 200, 430 },
                new int[] { 0, 45, 240, 480 });
        Route dl = route("SEED-HCM-DL", "TP. Hồ Chí Minh → Đà Lạt", 320000, RouteStatus.ACTIVE,
                List.of(mienTay, anSuong, daLat), new int[] { 0, 20, 310 }, new int[] { 0, 45, 420 });
        Route dn = route("SEED-HCM-DN", "TP. Hồ Chí Minh → Đà Nẵng", 520000, RouteStatus.ACTIVE,
                List.of(mienTay, nhaTrang, anNhon, daNang), new int[] { 0, 430, 670, 970 },
                new int[] { 0, 480, 825, 1170 });
        Route paused = route("SEED-HCM-BD-INACTIVE", "TP. Hồ Chí Minh → Bình Định (ngưng khai thác)", 350000,
                RouteStatus.INACTIVE, List.of(mienTay, anNhon), new int[] { 0, 670 }, new int[] { 0, 825 });
        var definitions = List.of(
                new SeedTrip(outbound, now.plusHours(2), ServiceType.BERTH, 34, 330000, TripStatus.SCHEDULED,
                        SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, now.plusHours(7), ServiceType.LIMOUSINE, 22, 460000, TripStatus.SCHEDULED,
                        SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.plusDays(1).atTime(1, 30), ServiceType.BERTH, 34, 310000,
                        TripStatus.SCHEDULED, SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.plusDays(1).atTime(7, 0), ServiceType.SEAT, 45, 270000,
                        TripStatus.SCHEDULED, SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.plusDays(1).atTime(10, 15), ServiceType.LIMOUSINE, 22, 490000,
                        TripStatus.SCHEDULED, SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.plusDays(1).atTime(13, 45), ServiceType.BERTH, 34, 350000,
                        TripStatus.SCHEDULED, SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.plusDays(1).atTime(18, 30), ServiceType.LIMOUSINE, 22, 520000,
                        TripStatus.SCHEDULED, SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.plusDays(1).atTime(22, 15), ServiceType.BERTH, 34, 390000,
                        TripStatus.SCHEDULED, SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(inbound, today.plusDays(1).atTime(8, 30), ServiceType.BERTH, 34, 360000,
                        TripStatus.SCHEDULED, SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(inbound, today.plusDays(1).atTime(20, 0), ServiceType.LIMOUSINE, 22, 470000,
                        TripStatus.SCHEDULED, SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(nt, today.plusDays(2).atTime(9, 0), ServiceType.BERTH, 34, 280000, TripStatus.SCHEDULED,
                        SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(dl, today.plusDays(2).atTime(15, 30), ServiceType.LIMOUSINE, 22, 360000,
                        TripStatus.SCHEDULED, SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(dn, today.plusDays(2).atTime(18, 0), ServiceType.BERTH, 34, 520000, TripStatus.SCHEDULED,
                        SaleStatus.OPEN, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.plusDays(1).atTime(12, 0), ServiceType.BERTH, 34, 350000, TripStatus.DRAFT,
                        SaleStatus.NOT_OPEN, VehicleStatus.MAINTENANCE),
                new SeedTrip(outbound, today.plusDays(1).atTime(14, 0), ServiceType.LIMOUSINE, 22, 450000,
                        TripStatus.SCHEDULED, SaleStatus.PAUSED, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.plusDays(1).atTime(16, 0), ServiceType.SEAT, 45, 290000,
                        TripStatus.SCHEDULED, SaleStatus.CLOSED, VehicleStatus.ACTIVE),
                new SeedTrip(inbound, today.atTime(0, 30), ServiceType.BERTH, 34, 340000, TripStatus.COMPLETED,
                        SaleStatus.CLOSED, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.atTime(0, 5), ServiceType.BERTH, 34, 370000, TripStatus.RUNNING,
                        SaleStatus.CLOSED, VehicleStatus.ACTIVE),
                new SeedTrip(outbound, today.plusDays(2).atTime(17, 0), ServiceType.LIMOUSINE, 22, 440000,
                        TripStatus.CANCELLED, SaleStatus.CLOSED, VehicleStatus.ACTIVE),
                new SeedTrip(paused, today.plusDays(1).atTime(11, 0), ServiceType.SEAT, 45, 290000, TripStatus.DRAFT,
                        SaleStatus.NOT_OPEN, VehicleStatus.INACTIVE));
        int inserted = 0;
        for (int i = 0; i < definitions.size(); i++) {
            String code = prefix + String.format("%02d", i + 1);
            if (tripRepository.existsByCode(code)) {
                continue;
            }
            var d = definitions.get(i);
            Vehicle vehicle = vehicle(i, d);
            int duration = routeStopRepository.findByRouteIdOrderByStopOrderAsc(d.route().getId())
                    .getLast()
                    .getOffsetMinutes();
            Trip trip = new Trip();
            trip.setCode(code);
            trip.setRoute(d.route());
            trip.setVehicle(vehicle);
            trip.setDepartureAt(d.departure());
            trip.setArrivalAt(d.departure().plusMinutes(duration));
            trip.setEstimatedDepartureAt(trip.getDepartureAt());
            trip.setEstimatedArrivalAt(trip.getArrivalAt());
            trip.setCapacitySnapshot(vehicle.getCapacity());
            trip.setTicketPrice(
                    BigDecimal.valueOf(java.util.concurrent.ThreadLocalRandom.current().nextInt(10, 100) * 100L));
            trip.setStatus(d.status());
            trip.setSaleStatus(d.saleStatus());
            if (d.status() != TripStatus.DRAFT) {
                trip.setPublishedAt(trip.getDepartureAt().minusDays(1));
            }
            if (d.status() == TripStatus.CANCELLED) {
                trip.setCancelledAt(trip.getDepartureAt().minusHours(1));
                trip.setCancelReason("Chuyến mẫu đã hủy");
            }
            if (d.status() == TripStatus.RUNNING || d.status() == TripStatus.COMPLETED) {
                trip.setActualDepartureAt(trip.getDepartureAt());
            }
            if (d.status() == TripStatus.COMPLETED) {
                trip.setActualArrivalAt(trip.getArrivalAt());
            }
            tripRepository.saveAndFlush(trip);
            if (d.status() != TripStatus.DRAFT) {
                for (int role = 0; role < 2; role++) {
                    TripDriver assignment = new TripDriver();
                    assignment.setTrip(trip);
                    assignment.setDriver(driver(i * 2 + role, today));
                    assignment.setRole(role == 0 ? RoleDriver.PRIMARY : RoleDriver.RELIEF);
                    tripDriverRepository.save(assignment);
                }
            }
            inserted++;
        }
        log.info("DatabaseSeeder: da tao {} chuyen mau con thieu, ma {}01 den {}20.", inserted, prefix, prefix);
    }

    private Station station(String name, String address, StationType type, StationStatus status) {
        return stationRepository.findAll()
                .stream()
                .filter(station -> station.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    Station station = stationRepository
                            .save(Station.builder().name(name).type(type).status(status).build());
                    stationAddressRepository.save(StationAddress.builder()
                            .station(station)
                            .address(address)
                            .effectiveFromAt(LocalDateTime.of(2020, 1, 1, 0, 0))
                            .build());
                    return station;
                });
    }

    // Tạo tuyến và chuỗi trạm tích lũy đúng chiều hành trình
    private Route route(String code, String name, int price, RouteStatus status, List<Station> points, int[] km,
            int[] minutes) {
        var existing = routeRepository.findAll()
                .stream()
                .filter(r -> r.getCode().equals(code) && r.getVersionNo() == 1)
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }
        Route route = routeRepository
                .saveAndFlush(Route.builder().code(code).versionNo(1).name(name).basePrice(BigDecimal.valueOf(price))
                        .effectiveFromAt(LocalDateTime.of(2020, 1, 1, 0, 0)).status(status).build());
        for (int i = 0; i < points.size(); i++) {
            boolean active = points.get(i).getStatus() == StationStatus.ACTIVE;
            routeStopRepository.save(RouteStop.builder()
                    .route(route)
                    .station(points.get(i))
                    .stopOrder(i + 1)
                    .distanceKm(BigDecimal.valueOf(km[i]))
                    .offsetMinutes(minutes[i])
                    .allowPickup(active && i < points.size() - 1)
                    .allowDropOff(active && i > 0)
                    .build());
        }
        routeStopRepository.flush();
        return route;
    }

    // Mỗi lượt có phương tiện riêng để tránh lịch xe bị chồng lấn
    private Vehicle vehicle(int index, SeedTrip definition) {
        String plate = String.format("77B-900.%02d", index + 1);
        return vehicleRepository.findAll()
                .stream()
                .filter(v -> v.getLicensePlate().equals(plate))
                .findFirst()
                .orElseGet(() -> vehicleRepository.save(Vehicle.builder()
                        .licensePlate(plate)
                        .type(definition.type())
                        .capacity(definition.capacity())
                        .status(definition.vehicleStatus())
                        .build()));
    }

    // Tạo cặp tài xế độc lập cho các chuyến đã được phân công
    private Driver driver(int index, LocalDate today) {
        String phone = String.format("0909%06d", index + 1);
        String[] names = {
                "Nguyễn Văn Minh", "Trần Quốc Hùng", "Lê Hoàng Nam", "Phạm Thanh Sơn",
                "Võ Đức Thành", "Đặng Văn Hải", "Bùi Hữu Phúc", "Nguyễn Minh Tuấn"
        };
        return driverRepository.findAll()
                .stream()
                .filter(d -> d.getPhone().equals(phone))
                .findFirst()
                .orElseGet(() -> driverRepository.save(Driver.builder()
                        .fullName(names[index % names.length] + " " + (index / 8 + 1))
                        .phone(phone)
                        .licenseNumber(String.format("SEED-GPLX-%04d", index + 1))
                        .licenseClass("D")
                        .licenseExpiryOn(today.plusYears(3))
                        .status(DriverStatus.ACTIVE)
                        .build()));
    }

    private record SeedTrip(
            Route route,
            LocalDateTime departure,
            ServiceType type,
            int capacity,
            int price,
            TripStatus status,
            SaleStatus saleStatus,
            VehicleStatus vehicleStatus
    ) {
    }
}
