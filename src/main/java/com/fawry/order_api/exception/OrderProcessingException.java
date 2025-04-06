package com.fawry.order_api.exception;

public class OrderProcessingException extends RuntimeException {
    public OrderProcessingException(String message, String e) {
        super(message);
    }
}
