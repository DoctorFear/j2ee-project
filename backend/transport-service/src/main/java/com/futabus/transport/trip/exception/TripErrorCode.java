package com.futabus.transport.trip.exception;

import com.futabus.transport.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripErrorCode implements ErrorCode {
    TRANSPORT_TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "Trip not found"),
    TRANSPORT_TRIP_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy chuyến xe theo mã đã cung cấp"),
    TRANSPORT_TRIP_INVALID_SCHEDULE(HttpStatus.BAD_REQUEST, "arrivalAt must be after departureAt"),
    TRANSPORT_TRIP_INVALID_STATUS(HttpStatus.BAD_REQUEST, "Trạng thái chuyến không hợp lệ"),
    TRANSPORT_TRIP_HAS_DRIVERS(HttpStatus.CONFLICT, "Delete the trip drivers before deleting this trip"),
    TRANSPORT_TRIP_DRIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "Trip driver not found"),
    TRANSPORT_TRIP_DRIVER_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "Driver already assigned to this trip");

    private final HttpStatus status;
    private final String message;
}
