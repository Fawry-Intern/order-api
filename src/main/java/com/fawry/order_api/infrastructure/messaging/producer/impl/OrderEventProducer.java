package com.fawry.order_api.infrastructure.messaging.producer.impl;

import com.fawry.order_api.domain.event.OrderCancelNotificationEvent;
import com.fawry.order_api.domain.event.OrderCreatedEventDTO;
import com.fawry.order_api.infrastructure.messaging.producer.OrderCancelEventPublisher;
import com.fawry.order_api.infrastructure.messaging.producer.OrderCreatedEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer <T>{

    private final OrderCreatedEventPublisher orderCreatedPublisher;
    private final OrderCancelEventPublisher orderCancellationPublisher;

   @Retryable(
           maxAttempts = 3,
           backoff = @Backoff(delay = 1000),
           retryFor = KafkaException.class,
           exceptionExpression = "!(#exception instanceof OrderNotFoundException)"
   )
    public void processEventProducer(T event, int orderHash) {
        if (event instanceof OrderCreatedEventDTO) {
            orderCreatedPublisher.publishOrderCreatedEvent((OrderCreatedEventDTO) event, orderHash);
        }else if (event instanceof OrderCancelNotificationEvent) {
            orderCancellationPublisher.publishOrderCanceledEvent((OrderCancelNotificationEvent) event, orderHash);
        }
    }

    @Recover
    public void recover(KafkaException e, T event, int orderHash) {
       log.error("Failed to publish event after 3 retries: {}. Event: {}", e.getMessage(), event);
       throw new RuntimeException("Failed to publish event to kafka after retries");
    }
}
