package com.fawry.order_api.infrastructure.messaging.producer;

import com.fawry.kafka.events.OrderCancelNotificationEvent;


public interface OrderCancelEventPublisher {
    void publishOrderCanceledEvent(OrderCancelNotificationEvent canceledEvent, int orderHash);
}
