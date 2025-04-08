package com.fawry.order_api.infrastructure.outbox;

import com.fawry.order_api.application.service.OutboxService;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.domain.model.Outbox;
import com.fawry.order_api.exception.OrderNotFoundException;
import com.fawry.order_api.infrastructure.repository.OrderRepository;
import com.fawry.order_api.infrastructure.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class OutboxPollingProcessor implements OutboxService {
    private final OutboxRepository outboxRepository;
    private final OrderRepository orderRepository;
    private final TransactionTemplate transactionTemplate;
    private final OutboxEventKafkaPublisher outboxEventKafkaPublisher;

    @Override
    @Scheduled(fixedDelayString = "${configuration.kafka.scheduled}")
    public void eventProcessing() {
        boolean isFailedProcessing = true;
        while (isFailedProcessing) {
            isFailedProcessing = Boolean.TRUE.equals(transactionTemplate.execute(this::doInTransaction));
        }
    }

    private Boolean doInTransaction(TransactionStatus status) {
        List<Outbox> outboxes = outboxRepository.findTop10ByProcessedOrderByCreatedAt(Boolean.FALSE)
                .orElse(Collections.emptyList());

        if (!outboxes.isEmpty()) {
            List<CompletableFuture<Long>> futures = new ArrayList<>();

            for (Outbox outbox : outboxes) {
                try {
                    Order order = orderRepository.findWithOrderItemsById(outbox.getOrderId())
                            .orElseThrow(() -> new OrderNotFoundException(outbox.getOrderId()));
                    futures.add(outboxEventKafkaPublisher.sendEventToKafkaAsync(outbox, order));
                } catch (Exception e) {
                    log.error("Failed to prepare event for Kafka: {}. Error: {}", outbox.getId(), e.getMessage());
                }
            }

            List<Long> successfulIds = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!successfulIds.isEmpty()) {
                outboxRepository.updateProcessedByIds(Boolean.TRUE, successfulIds);
                return true;
            }
        }

        return false;
    }

}
