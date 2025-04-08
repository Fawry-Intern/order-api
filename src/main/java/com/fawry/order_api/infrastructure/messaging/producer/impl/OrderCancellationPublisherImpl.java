package com.fawry.order_api.infrastructure.messaging.producer.impl;

import com.fawry.order_api.domain.event.OrderCancelNotificationEvent;
import com.fawry.order_api.infrastructure.messaging.producer.OrderCancelEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import static org.springframework.kafka.support.KafkaHeaders.PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

@Service
@RequiredArgsConstructor
public class OrderCancellationPublisherImpl implements OrderCancelEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${custom.order.topic.name}")
    private String TOPIC_NAME;
    @Value("${custom.order.topic.total_partitions}")
    private int TOTAL_PARTITIONS;

    public void publishOrderCanceledEvent(OrderCancelNotificationEvent canceledEvent, int orderHash) {
        Message<OrderCancelNotificationEvent> message =
                MessageBuilder
                        .withPayload(canceledEvent)
                        .setHeader(TOPIC, TOPIC_NAME)
                        .setHeader(PARTITION, 3)
                        .build();
        kafkaTemplate.send(message);
    }

}
