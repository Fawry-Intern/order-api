package com.fawry.order_api.exception;

public class InvalidOrderException extends OrderApiException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
