package com.fawry.kafka.events;

public abstract class OrderSagaEvent {

    private final Long orderId;

    protected OrderSagaEvent(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
