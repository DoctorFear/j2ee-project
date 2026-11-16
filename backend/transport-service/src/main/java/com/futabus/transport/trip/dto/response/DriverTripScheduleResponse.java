// DTO thông tin chuyến xe trong lịch ca trực tài xế
package com.futabus.transport.trip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverTripScheduleResponse {
    private String tripCode;
    private String routeCode;
    private String routeName;
    private String licensePlate;
    private String vehicleType;
    private int capacity;
    private LocalDateTime departureAt;
    private LocalDateTime arrivalAt;
    private LocalDateTime actualDepartureAt;
    private LocalDateTime actualArrivalAt;
    private String tripStatus; // SCHEDULED, RUNNING, COMPLETED, CANCELLED
    private String driverRole; // PRIMARY / RELIEF
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String driverLicenseNumber;
    private int bookedCount;
    private int boardedCount;
    private List<DriverTripStopDto> stops;
}
