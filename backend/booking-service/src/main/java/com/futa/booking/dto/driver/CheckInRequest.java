package com.futa.booking.dto.driver;

import lombok.Data;

@Data
public class CheckInRequest {
    private String qrToken;
    private String ticketCode;
    private String last4Phone; // Dùng khi tài xế nhập thủ công 4 số cuối SĐT
}