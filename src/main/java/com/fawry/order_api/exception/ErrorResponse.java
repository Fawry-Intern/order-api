package com.fawry.order_api.exception;

import com.fawry.order_api.dto.enums.ErrorCode;

import java.time.Instant;

public class ErrorResponse {
    private final String message;
    private final ErrorCode errorCode;
    private final Instant timestamp;

    public ErrorResponse(String message, ErrorCode errorCode) {
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
    }

    public String getMessage() {
        return message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}