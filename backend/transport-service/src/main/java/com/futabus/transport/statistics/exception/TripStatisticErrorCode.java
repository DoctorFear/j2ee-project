package com.futabus.transport.statistics.exception;

import com.futabus.transport.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TripStatisticErrorCode implements ErrorCode {
    TRANSPORT_TRIP_STATISTIC_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Trip statistic not found"
    ),
    ;

    private final HttpStatus status;
    private final String message;
}
