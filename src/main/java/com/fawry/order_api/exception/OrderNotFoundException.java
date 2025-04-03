package com.fawry.order_api.exception;

public class OrderNotFoundException extends OrderApiException {
    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
    }
}
