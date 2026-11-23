package com.futabus.transport.trip.exception;

import com.futabus.transport.common.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum TripErrorCode implements ErrorCode {
    TRANSPORT_TRIP_CODE_EXISTS(
            HttpStatus.CONFLICT,
            "Trip code already exists"
    ),
    TRANSPORT_TRIP_TRANSITION_INVALID(
            HttpStatus.CONFLICT,
            "Trip status does not allow this operation"
    ),
    TRANSPORT_TRIP_VEHICLE_REQUIRED(
            HttpStatus.CONFLICT,
            "Assign an active vehicle before publishing"
    ),
    TRANSPORT_TRIP_VEHICLE_CHANGE_BLOCKED(
            HttpStatus.CONFLICT,
            "Vehicle may only change before the trip has opened for sale"
    ),
    TRANSPORT_TRIP_PRIMARY_REQUIRED(
            HttpStatus.CONFLICT,
            "Trip must have one primary driver"
    ),
    TRANSPORT_TRIP_PRIMARY_EXISTS(
            HttpStatus.CONFLICT,
            "Trip already has a primary driver"
    ),
    TRANSPORT_TRIP_DRIVER_INELIGIBLE(
            HttpStatus.CONFLICT,
            "Driver needs an active profile"
    ),
    TRANSPORT_TRIP_DISPATCH_CONFLICT(
            HttpStatus.CONFLICT,
            "Vehicle or driver already has a conflicting trip"
    ),
    TRANSPORT_TRIP_INVALID_ESTIMATE(
            HttpStatus.BAD_REQUEST,
            "Estimated arrival must be after departure"
    ),
    TRANSPORT_TRIP_SALE_TRANSITION_INVALID(
            HttpStatus.CONFLICT,
            "Trip sale status does not allow this transition"
    ),
    TRANSPORT_TRIP_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Trip not found"
    ),
    TRANSPORT_TRIP_CODE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy chuyến xe theo mã đã cung cấp"
    ),
    TRANSPORT_TRIP_INVALID_SCHEDULE(
            HttpStatus.BAD_REQUEST,
            "arrivalAt must be after departureAt"
    ),
    TRANSPORT_TRIP_INVALID_STATUS(
            HttpStatus.BAD_REQUEST,
            "Trạng thái chuyến không hợp lệ"
    ),
    TRANSPORT_TRIP_HAS_DRIVERS(
            HttpStatus.CONFLICT,
            "Delete the trip drivers before deleting this trip"
    ),
    TRANSPORT_TRIP_DRIVER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Trip driver not found"
    ),
    TRANSPORT_TRIP_DRIVER_ALREADY_ASSIGNED(
            HttpStatus.CONFLICT,
            "Driver already assigned to this trip"
    ),
    ;

    HttpStatus status;
    String message;
}
