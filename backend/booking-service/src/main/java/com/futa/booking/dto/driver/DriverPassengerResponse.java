package com.futa.booking.dto.driver;

import lombok.Data;
import java.time.Instant;

@Data
public class DriverPassengerResponse {
    private String ticketCode;
    private String seatCode;       // Ví dụ: A01, B12
    private String floor;          // Tầng 1 (Dãy A) / Tầng 2 (Dãy B)
    private String customerName;
    private String customerPhone;
    private String pickupStationId;
    private String dropOffStationId;
    private boolean isBoarded;
    private Instant boardedAt;
    private String ticketStatus;   // PAID
}