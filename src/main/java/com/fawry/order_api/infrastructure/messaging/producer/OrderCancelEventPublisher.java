package com.fawry.order_api.infrastructure.messaging.producer;

import com.fawry.order_api.domain.event.OrderCancelNotificationEvent;


public interface OrderCancelEventPublisher {
    void publishOrderCanceledEvent(OrderCancelNotificationEvent canceledEvent, int orderHash);
}
