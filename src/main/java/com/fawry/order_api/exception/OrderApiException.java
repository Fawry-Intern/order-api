package com.fawry.order_api.exception;

public class OrderApiException extends RuntimeException{
    public OrderApiException(String message) {
        super(message);
    }

    public OrderApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
