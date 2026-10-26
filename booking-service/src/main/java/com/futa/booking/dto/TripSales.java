// Số vé và doanh thu đối soát theo chuyến
package com.futa.booking.dto;

import java.math.BigDecimal;

public record TripSales(
    String tripCode,
    long bookedCount,
    long refundedCount,
    BigDecimal grossRevenue,
    BigDecimal refundedAmount,
    BigDecimal netRevenue) {}
