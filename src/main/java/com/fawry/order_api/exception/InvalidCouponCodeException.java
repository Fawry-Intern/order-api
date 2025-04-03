package com.fawry.order_api.exception;

public class InvalidCouponCodeException extends OrderApiException {
    public InvalidCouponCodeException(String message) {
        super(message);
    }
}
