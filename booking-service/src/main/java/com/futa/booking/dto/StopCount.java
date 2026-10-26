// Số hành khách đón và trả tại từng trạm
package com.futa.booking.dto;

public record StopCount(String stationId, long pickupCount, long dropOffCount) {}
