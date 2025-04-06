package com.fawry.order_api.application.usecase;

import com.fawry.kafka.events.OrderCreatedEventDTO;
import com.fawry.order_api.application.service.OutboxService;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.domain.model.Outbox;
import com.fawry.order_api.exception.OrderNotFoundException;
import com.fawry.order_api.infrastructure.messaging.producer.impl.OrderEventProducer;
import com.fawry.order_api.infrastructure.repository.OrderRepository;
import com.fawry.order_api.infrastructure.repository.OutboxRepository;
import com.fawry.order_api.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceUseCase implements OutboxService {
    private final OrderEventProducer<Object> producer;
    private final OutboxRepository outboxRepository;
    private final OutboxMapper outboxMapper;
    private final OrderRepository orderRepository;

    @Override
    @Scheduled(fixedDelay = 5000)
    public void eventProcessing() {
        List<Outbox> listOrOutboxEventEntities = new ArrayList<>();
        outboxRepository.findAll().forEach(listOrOutboxEventEntities::add);
        log.info("Number of outbox events: {}", listOrOutboxEventEntities.size());

        if (!listOrOutboxEventEntities.isEmpty()) {
            for (Outbox outbox : listOrOutboxEventEntities) {
                Order order = orderRepository.findWithOrderItemsById(outbox.getOrderId())
                                .orElseThrow(() -> new OrderNotFoundException(outbox.getOrderId()));
                sendEventToKafka(outbox, order);
                outboxRepository.deleteById(outbox.getId());
            }
        }
    }

    private void sendEventToKafka(Outbox outbox, Order order) {
        OrderCreatedEventDTO orderCreatedEventDTO = outboxMapper.mapToOrderCreatedEventDTO(outbox, order);
        log.info("Processing to poll OutboxEvent from database to kafka: {}", orderCreatedEventDTO);
        producer.processEventProducer(orderCreatedEventDTO, order.hashCode());
    }
}
