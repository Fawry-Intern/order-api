package com.fawry.order_api.infrastructure.messaging.producer;

import com.fawry.kafka.events.OrderCreatedEventDTO;

public interface OrderCreatedEventPublisher {
    void publishOrderCreatedEvent(OrderCreatedEventDTO createdEvent, int orderHash);
}
