// DTO tổng hợp danh sách hành khách và tiến độ đón trả của chuyến
package com.futa.booking.dto.driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverTripSummaryResponse {
    private String tripCode;
    private int totalPassengers;
    private int totalBoarded;
    private List<DriverPassengerResponse> passengers;
}
