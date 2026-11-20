package com.futabus.transport.route.exception;

import com.futabus.transport.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RouteErrorCode implements ErrorCode {
    TRANSPORT_ROUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "Route not found"),
    TRANSPORT_ROUTE_VERSION_EXISTS(HttpStatus.CONFLICT, "Route code and version already exist"),
    TRANSPORT_ROUTE_HAS_STOPS(HttpStatus.CONFLICT, "Delete the route stops before deleting this route"),
    TRANSPORT_ROUTE_INVALID_EFFECTIVE_PERIOD(HttpStatus.BAD_REQUEST, "effectiveToAt must be after effectiveFromAt"),
    TRANSPORT_ROUTE_STOP_NOT_FOUND(HttpStatus.NOT_FOUND, "Route stop not found"),
    TRANSPORT_ROUTE_STOP_ORDER_EXISTS(HttpStatus.CONFLICT, "Stop order already exists in this route"),
    TRANSPORT_STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Station not found");

    private final HttpStatus status;
    private final String message;
}
