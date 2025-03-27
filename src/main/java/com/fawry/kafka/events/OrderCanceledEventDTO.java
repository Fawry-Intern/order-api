package com.fawry.kafka.events;

public class OrderCanceledEventDTO extends OrderSagaEvent{

    private final String reason;
    private final String customerEmail;

    public OrderCanceledEventDTO(Long orderId, String reason, String customerEmail) {
        super(orderId);
        this.reason = reason;
        this.customerEmail = customerEmail;
    }

    public static OrderCanceledEventDTO newInstance(Long orderId, String reason, String customerEmail) {
        return new OrderCanceledEventDTO(orderId, reason, customerEmail);
    }
    @Override
    public String toString() {
        return "OrderCanceledEventDTO{" +
                "reason='" + reason + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                '}';
    }
}
