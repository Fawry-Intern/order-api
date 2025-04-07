package com.fawry.order_api.infrastructure.outbox;

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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class ScheduledOutboxEventProcessor implements OutboxService {
    private final OrderEventProducer<Object> producer;
    private final OutboxRepository outboxRepository;
    private final OutboxMapper outboxMapper;
    private final OrderRepository orderRepository;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Scheduled(fixedDelayString = "${configuration.kafka.scheduled}")
    public void eventProcessing() {
        boolean isFailedProcessing = true;
        while (isFailedProcessing) {
            isFailedProcessing = Boolean.TRUE.equals(transactionTemplate.execute(this::doInTransaction));
        }

    }

    private Boolean doInTransaction(TransactionStatus status) {
        List<Outbox> outboxes = outboxRepository.findTop10ByProcessed(Boolean.FALSE)
                .orElse(Collections.emptyList());

        if (!outboxes.isEmpty()) {
            for (Outbox outbox : outboxes) {
                Order order = orderRepository.findWithOrderItemsById(outbox.getOrderId())
                        .orElseThrow(() -> new OrderNotFoundException(outbox.getOrderId()));
                sendEventToKafka(outbox, order);
                outbox.setProcessed(Boolean.TRUE);
                outboxRepository.save(outbox);
            }
            return true;
        }
        return false;
    }

    private void sendEventToKafka(Outbox outbox, Order order) {
        OrderCreatedEventDTO orderCreatedEventDTO = outboxMapper.mapToOrderCreatedEventDTO(outbox, order);
        log.info("Processing to poll OutboxEvent from database to kafka: {}", orderCreatedEventDTO);
        producer.processEventProducer(orderCreatedEventDTO, order.hashCode());
    }
}
