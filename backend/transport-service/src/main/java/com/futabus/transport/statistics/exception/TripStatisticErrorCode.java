package com.futabus.transport.statistics.exception;

import com.futabus.transport.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripStatisticErrorCode implements ErrorCode {
    TRANSPORT_TRIP_STATISTIC_NOT_FOUND(HttpStatus.NOT_FOUND, "Trip statistic not found"),
    TRANSPORT_TRIP_STATISTIC_ALREADY_EXISTS(HttpStatus.CONFLICT, "Trip statistic already exists for this trip");

    private final HttpStatus status;
    private final String message;
}
