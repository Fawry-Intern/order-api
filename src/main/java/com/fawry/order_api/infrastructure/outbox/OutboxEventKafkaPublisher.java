package com.fawry.order_api.infrastructure.outbox;

import com.fawry.order_api.domain.event.OrderCreatedEventDTO;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.domain.model.Outbox;
import com.fawry.order_api.infrastructure.messaging.producer.impl.OrderEventProducer;
import com.fawry.order_api.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventKafkaPublisher {
    private final OrderEventProducer<Object> producer;
    private final OutboxMapper outboxMapper;

    @Async("outboxEventProcessorExecutor")
    public CompletableFuture<Long> sendEventToKafkaAsync(Outbox outbox, Order order) {
        try {
            OrderCreatedEventDTO orderCreatedEventDTO = outboxMapper.mapToOrderCreatedEventDTO(outbox, order);
            log.info("Processing to poll OutboxEvent from database to kafka: {}", orderCreatedEventDTO);
            producer.processEventProducer(orderCreatedEventDTO, order.hashCode());

            return CompletableFuture.completedFuture(outbox.getId());
        } catch (Exception e) {
            log.error("Failed to send to Kafka: {}. Error: {}", outbox.getId(), e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }
}
