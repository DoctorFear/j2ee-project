package com.futabus.transport.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    COMMON_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    COMMON_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Invalid request"),
    COMMON_INVALID_JSON(HttpStatus.BAD_REQUEST, "Invalid JSON request"),
    COMMON_INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid parameter"),
    COMMON_MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "Required parameter is missing"),
    COMMON_NOT_FOUND(HttpStatus.NOT_FOUND, "Endpoint not found"),
    COMMON_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method is not allowed"),
    COMMON_NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Requested response format is not supported"),
    COMMON_UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported request content type"),
    COMMON_DATA_CONFLICT(HttpStatus.CONFLICT, "Request conflicts with existing data or references"),
    COMMON_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "Request failed"),
    COMMON_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus status;
    private final String message;
}
