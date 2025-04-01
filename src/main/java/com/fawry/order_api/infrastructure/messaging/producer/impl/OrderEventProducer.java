package com.fawry.order_api.infrastructure.messaging.producer.impl;

import com.fawry.kafka.events.OrderCancelNotificationEvent;
import com.fawry.kafka.events.OrderCreatedEventDTO;
import com.fawry.order_api.infrastructure.messaging.producer.OrderCancelEventPublisher;
import com.fawry.order_api.infrastructure.messaging.producer.OrderCreatedEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer <T>{

    private final OrderCreatedEventPublisher orderCreatedPublisher;
    private final OrderCancelEventPublisher orderCancellationPublisher;

    public void processEventProducer(T event, int orderHash) {
        log.info("Event processing{}", event);
        if (event instanceof OrderCreatedEventDTO) {
            orderCreatedPublisher.publishOrderCreatedEvent((OrderCreatedEventDTO) event, orderHash);
        }else if (event instanceof OrderCancelNotificationEvent) {
            orderCancellationPublisher.publishOrderCanceledEvent((OrderCancelNotificationEvent) event, orderHash);
        }
    }
}
