package com.fawry.order_api.infrastructure.messaging.consumer;

import com.fawry.kafka.events.OrderCanceledEventDTO;
import com.fawry.order_api.domain.service.saga.OrderCompensationSagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCancellationConsumer {

    private final OrderCompensationSagaService orderCompensationSagaService;

    @KafkaListener(topics = "store-events", groupId = "order_store_id")
    public void consumeStoreCancellation(OrderCanceledEventDTO orderCanceledEventDTO) {

        log.info("Store cancellation process successfully {}", orderCanceledEventDTO);

        orderCompensationSagaService.compensateOrder(orderCanceledEventDTO.getOrderId(), orderCanceledEventDTO.getReason(), orderCanceledEventDTO.getCustomerEmail());
    }
}
