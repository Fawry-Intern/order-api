package com.fawry.order_api.exception;

public class CouponUnavailabilityException extends OrderApiException {

    public CouponUnavailabilityException(String message) {
        super(message);
    }
}
