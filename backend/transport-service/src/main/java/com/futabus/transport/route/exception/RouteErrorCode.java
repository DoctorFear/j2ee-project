package com.futabus.transport.route.exception;

import com.futabus.transport.common.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum RouteErrorCode implements ErrorCode {
    TRANSPORT_ROUTE_PERIOD_OVERLAP(
            HttpStatus.CONFLICT,
            "Route versions with the same code must have separate effective periods"
    ),
    TRANSPORT_ROUTE_PINNED(
            HttpStatus.CONFLICT,
            "Route version is already used by a trip"
    ),
    TRANSPORT_ROUTE_NOT_EFFECTIVE(
            HttpStatus.CONFLICT,
            "Route is not active and effective at departure"
    ),
    TRANSPORT_ROUTE_INVALID_ITINERARY(
            HttpStatus.BAD_REQUEST,
            "Route must have an ordered itinerary with valid cumulative measurements and endpoint permissions"
    ),
    TRANSPORT_ROUTE_STOP_INVALID_ORIGIN(
            HttpStatus.BAD_REQUEST,
            "The first stop must have zero distance and offset"
    ),
    TRANSPORT_SEGMENT_SAME_STATION(
            HttpStatus.BAD_REQUEST,
            "Segment origin and destination must differ"
    ),
    TRANSPORT_SEGMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Station segment not found"
    ),
    TRANSPORT_ROUTE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Route not found"
    ),
    TRANSPORT_ROUTE_VERSION_EXISTS(
            HttpStatus.CONFLICT,
            "Route code and version already exist"
    ),
    TRANSPORT_ROUTE_HAS_STOPS(
            HttpStatus.CONFLICT,
            "Delete the route stops before deleting this route"
    ),
    TRANSPORT_ROUTE_INVALID_EFFECTIVE_PERIOD(
            HttpStatus.BAD_REQUEST,
            "effectiveToAt must be after effectiveFromAt"
    ),
    TRANSPORT_ROUTE_STOP_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Route stop not found"
    ),
    TRANSPORT_ROUTE_STOP_ORDER_EXISTS(
            HttpStatus.CONFLICT,
            "Stop order already exists in this route"
    ),
    TRANSPORT_STATION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Station not found"
    ),
    ;

    HttpStatus status;
    String message;
}
