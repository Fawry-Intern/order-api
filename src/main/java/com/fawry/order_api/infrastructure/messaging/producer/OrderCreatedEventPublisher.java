package com.fawry.order_api.infrastructure.messaging.producer;

import com.fawry.order_api.domain.event.OrderCreatedEventDTO;

public interface OrderCreatedEventPublisher {
    void publishOrderCreatedEvent(OrderCreatedEventDTO createdEvent, int orderHash);
}
