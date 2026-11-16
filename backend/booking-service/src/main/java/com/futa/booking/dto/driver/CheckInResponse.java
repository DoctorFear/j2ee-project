package com.futa.booking.dto.driver;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class CheckInResponse {
    private boolean success;
    private String message;
    private String ticketCode;
    private String seatCode;
    private String customerName;
    private String customerPhone;
    private String pickupStationName;
    private String dropOffStationName;
    private Instant boardedAt;
    private int totalBoarded;
    private int totalPassengers;
}