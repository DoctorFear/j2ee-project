package com.futabus.transport.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    // The enum constant name is the public error code returned by the API.
    String name();

    HttpStatus getStatus();

    String getMessage();
}
