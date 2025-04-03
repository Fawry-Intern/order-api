package com.fawry.order_api.exception;

public class InvalidDiscountException extends OrderApiException{
    public InvalidDiscountException(String message) {
        super(message);
    }
}
