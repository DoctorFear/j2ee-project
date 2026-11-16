// DTO trạm dừng trong hành trình chuyến xe của tài xế
package com.futabus.transport.trip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverTripStopDto {
    private Long stationId;
    private String stationName;
    private String stationAddress;
    private int stopOrder;
    private int offsetMinutes;
    private LocalDateTime estimatedTime;
    private boolean allowPickup;
    private boolean allowDropOff;
    private int pickupCount;
    private int dropOffCount;
}
