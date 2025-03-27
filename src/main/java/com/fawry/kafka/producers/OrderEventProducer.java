package com.fawry.kafka.producers;

import com.fawry.kafka.events.OrderCanceledEventDTO;
import com.fawry.kafka.events.OrderCreatedEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static org.springframework.kafka.support.KafkaHeaders.PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${custom.order.topic.name}")
    private String TOPIC_NAME;
    @Value("${custom.order.topic.total_partitions}")
    private int TOTAL_PARTITIONS;

    public void publishOrderCreatedEvent(OrderCreatedEventDTO createdEvent, int orderHash) {
        Message<OrderCreatedEventDTO> message =
                MessageBuilder
                        .withPayload(createdEvent)
                        .setHeader(TOPIC, TOPIC_NAME)
                        .setHeader(PARTITION, 0)
                        .build();
        kafkaTemplate.send(message);
    }

    public void publishOrderCanceledEvent(OrderCanceledEventDTO canceledEvent, int orderHash) {
        Message<OrderCanceledEventDTO> message =
                MessageBuilder
                        .withPayload(canceledEvent)
                        .setHeader(TOPIC, TOPIC_NAME)
                        .setHeader(PARTITION, 3)
                        .build();
        kafkaTemplate.send(message);
    }

    private int randomPartition(int hash) {
        return hash % TOTAL_PARTITIONS;
    }
}
