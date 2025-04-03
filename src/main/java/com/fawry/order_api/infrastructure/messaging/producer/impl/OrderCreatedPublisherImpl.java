package com.fawry.order_api.infrastructure.messaging.producer.impl;

import com.fawry.kafka.events.OrderCreatedEventDTO;
import com.fawry.order_api.infrastructure.messaging.producer.OrderCreatedEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import static org.springframework.kafka.support.KafkaHeaders.PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.TOPIC;


@Service
@RequiredArgsConstructor
public class OrderCreatedPublisherImpl implements OrderCreatedEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedPublisherImpl.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${custom.order.topic.name}")
    private String TOPIC_NAME;
    @Value("${custom.order.topic.total_partitions}")
    private int TOTAL_PARTITIONS;

    @Override
    public void publishOrderCreatedEvent(OrderCreatedEventDTO createdEvent, int orderHash) {
        log.info("Publish order event created to store to reserve the orderItems {}: ", createdEvent);
        Message<OrderCreatedEventDTO> message =
                MessageBuilder
                        .withPayload(createdEvent)
                        .setHeader(TOPIC, TOPIC_NAME)
                        .setHeader(PARTITION, 0)
                        .build();
        kafkaTemplate.send(message);
    }


}
