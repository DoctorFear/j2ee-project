package com.futabus.transport.resource.exception;

import com.futabus.transport.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResourceErrorCode implements ErrorCode {
    TRANSPORT_VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Vehicle not found"),
    TRANSPORT_DRIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "Driver not found");

    private final HttpStatus status;
    private final String message;
}
