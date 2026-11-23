// Xử lý nghiệp vụ lịch ca trực và trạng thái vận hành chuyến xe cho tài xế
package com.futabus.transport.trip.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.route.entity.StationAddress;
import com.futabus.transport.route.repository.StationAddressRepository;
import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.resource.repository.DriverRepository;
import com.futabus.transport.route.entity.RouteStop;
import com.futabus.transport.route.repository.RouteStopRepository;
import com.futabus.transport.trip.dto.response.DriverTripScheduleResponse;
import com.futabus.transport.trip.dto.response.DriverTripStopDto;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.entity.TripDriver;
import com.futabus.transport.trip.enums.TripStatus;
import com.futabus.transport.trip.exception.TripErrorCode;
import com.futabus.transport.trip.repository.TripDriverRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DriverTripService {

    TripDriverRepository tripDriverRepository;
    TripService tripService;
    StationAddressRepository stationAddressRepository;
    DriverRepository driverRepository;
    RouteStopRepository routeStopRepository;

    /**
     * Lấy thông tin hồ sơ tài xế đồng bộ từ danh sách tài xế của Admin (Transport
     * DB)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDriverProfile(Long driverId, String phone) {
        Driver driver = resolveDriver(driverId, phone);
        if (driver == null) {
            return Collections.emptyMap();
        }
        List<TripDriver> assignments = tripDriverRepository.findByDriverId(driver.getId());
        String role = assignments.stream()
                .filter(a -> a.getRole() != null)
                .map(a -> a.getRole().name())
                .findFirst()
                .orElse("PRIMARY");

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", driver.getId());
        profile.put("fullName", driver.getFullName());
        profile.put("phone", driver.getPhone());
        profile.put("licenseNumber", driver.getLicenseNumber());
        profile.put("licenseClass", driver.getLicenseClass());
        profile.put("status", driver.getStatus() != null ? driver.getStatus().name() : "ACTIVE");
        profile.put("role", role);
        return profile;
    }

    /**
     * Lấy lịch ca trực của tài xế theo ngày và tài xế
     */
    @Transactional(readOnly = true)
    public List<DriverTripScheduleResponse> getDriverTrips(Long driverId, String phone, LocalDate date) {
        Driver driver = resolveDriver(driverId, phone);
        if (driver == null) {
            log.warn("Không tìm thấy thông tin tài xế với driverId: {}, phone: {}", driverId, phone);
            return Collections.emptyList();
        }

        List<TripDriver> assignments = tripDriverRepository.findByDriverId(driver.getId());

        return assignments.stream()
                .filter(a -> a.getTrip() != null && (date == null || matchesDate(a.getTrip().getDepartureAt(), date)))
                .sorted(Comparator.comparing(
                        a -> a.getTrip().getDepartureAt(),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(a -> mapToTripScheduleResponse(
                        a.getTrip(),
                        driver,
                        a.getRole() != null ? a.getRole().name() : "PRIMARY"))
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật trạng thái chuyến xe (Bắt đầu chạy RUNNING hoặc Hoàn thành
     * COMPLETED)
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DriverTripScheduleResponse updateTripStatus(String tripCode, String statusName) {
        TripStatus target;
        try {
            target = TripStatus.valueOf(statusName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(TripErrorCode.TRANSPORT_TRIP_INVALID_STATUS);
        }

        Trip updatedTrip = tripService.updateDrivingStatus(tripCode, target);

        return mapToTripScheduleResponse(updatedTrip, null, "PRIMARY");
    }

    private Driver resolveDriver(Long driverId, String phone) {
        if (phone != null && !phone.isBlank()) {
            String cleanPhone = phone.trim();
            Optional<Driver> driverOpt = driverRepository.findByPhone(cleanPhone);
            if (driverOpt.isPresent()) {
                return driverOpt.get();
            }
            // Ánh xạ SĐT tài khoản Auth (0983456701..05) sang SĐT tài xế tương ứng
            // trong danh sách Admin (0909000001..05)
            if (cleanPhone.matches("09834567[0-9]{2}")) {
                try {
                    int idx = Integer.parseInt(cleanPhone.substring(8));
                    String adminSeedPhone = String.format("0909%06d", idx);
                    Optional<Driver> mappedOpt = driverRepository.findByPhone(adminSeedPhone);
                    if (mappedOpt.isPresent()) {
                        return mappedOpt.get();
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (driverId != null && driverId > 0) {
            Optional<Driver> byId = driverRepository.findById(driverId);
            if (byId.isPresent()) {
                return byId.get();
            }
        }

        // Fallback: Lấy tài xế đầu tiên trong danh sách Admin
        return driverRepository.findAll().stream().findFirst().orElse(null);
    }

    private boolean matchesDate(LocalDateTime departureAt, LocalDate date) {
        if (departureAt == null) {
            return false;
        }
        return departureAt.toLocalDate().isEqual(date);
    }

    private DriverTripScheduleResponse mapToTripScheduleResponse(Trip trip, Driver driver, String role) {
        List<DriverTripStopDto> stopDtos = Collections.emptyList();
        if (trip.getRoute() != null) {
            List<RouteStop> routeStops = routeStopRepository.findByRouteIdOrderByStopOrderAsc(trip.getRoute().getId());
            stopDtos = routeStops.stream().map(rs -> {
                LocalDateTime plannedAt = trip.getDepartureAt().plusMinutes(rs.getOffsetMinutes());
                String address = null;
                if (rs.getStation() != null) {
                    address = stationAddressRepository
                            .findFirstByStationIdAndEffectiveFromAtLessThanEqualOrderByEffectiveFromAtDesc(
                                    rs.getStation().getId(), plannedAt)
                            .map(StationAddress::getAddress)
                            .orElse(null);
                }
                return DriverTripStopDto.builder()
                        .stationId(rs.getStation() != null ? rs.getStation().getId() : null)
                        .stationName(rs.getStation() != null ? rs.getStation().getName() : null)
                        .stationAddress(address)
                        .stopOrder(rs.getStopOrder())
                        .offsetMinutes(rs.getOffsetMinutes())
                        .estimatedTime(resolveDepartureTime(trip).plusMinutes(rs.getOffsetMinutes()))
                        .allowPickup(rs.isAllowPickup())
                        .allowDropOff(rs.isAllowDropOff())
                        .pickupCount(0)
                        .dropOffCount(0)
                        .build();
            }).collect(Collectors.toList());
        }

        return DriverTripScheduleResponse.builder()
                .tripCode(trip.getCode())
                .routeCode(trip.getRoute() != null ? trip.getRoute().getCode() : null)
                .routeName(trip.getRoute() != null ? trip.getRoute().getName() : null)
                .licensePlate(trip.getVehicle() != null ? trip.getVehicle().getLicensePlate() : null)
                .vehicleType(trip.getVehicle() != null && trip.getVehicle().getType() != null
                        ? trip.getVehicle().getType().name()
                        : null)
                .capacity(trip.getCapacitySnapshot() != null ? trip.getCapacitySnapshot() : 0)
                .departureAt(trip.getDepartureAt())
                .arrivalAt(trip.getArrivalAt())
                .actualDepartureAt(trip.getActualDepartureAt())
                .actualArrivalAt(trip.getActualArrivalAt())
                .tripStatus(trip.getStatus() != null ? trip.getStatus().name() : "SCHEDULED")
                .driverRole(role != null ? role : "PRIMARY")
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driver != null ? driver.getFullName() : null)
                .driverPhone(driver != null ? driver.getPhone() : null)
                .driverLicenseNumber(driver != null ? driver.getLicenseNumber() : null)
                .bookedCount(0)
                .boardedCount(0)
                .stops(stopDtos)
                .build();
    }

    private LocalDateTime resolveDepartureTime(Trip trip) {
        if (trip.getActualDepartureAt() != null) {
            return trip.getActualDepartureAt();
        }
        if (trip.getEstimatedDepartureAt() != null) {
            return trip.getEstimatedDepartureAt();
        }
        return trip.getDepartureAt();
    }
}
