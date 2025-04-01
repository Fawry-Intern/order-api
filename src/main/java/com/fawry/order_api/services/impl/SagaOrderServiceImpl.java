package com.fawry.order_api.services.impl;

import com.fawry.kafka.events.OrderCanceledEventDTO;
import com.fawry.kafka.producers.OrderEventProducer;
import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.dto.dtos.OrderResponse;
import com.fawry.order_api.dto.enums.OrderSagaStatus;
import com.fawry.order_api.entities.Order;
import com.fawry.order_api.mapper.OrderMapper;
import com.fawry.order_api.repositories.OrderRepository;
import com.fawry.order_api.services.OrderDiscountService;
import com.fawry.order_api.services.OrderService;
import com.fawry.order_api.services.OrderCreationSaga;
import com.fawry.order_api.services.OrderCancellationSaga;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrderServiceImpl implements OrderCreationSaga, OrderCancellationSaga, OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderDiscountService couponService;
    private final PlatformTransactionManager transactionManager;
    private final OrderEventProducer producer;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        var order = newInstance(request);
        saveOrderWithTransaction(order);

        if (request.couponCode() == null) {
            throw new IllegalArgumentException("Can not to apply coupon in null code");
        }
        applyCouponWithTransaction(order);

        var orderCreatedEvent = mapper.mapFromOrderToOrderCreatedEvent(order,
                request.customerEmail(),
                request.customerName(),
                request.customerContact(),
                request.addressDetails(),
                request.paymentMethod());
        producer.publishOrderCreatedEvent(orderCreatedEvent, order.hashCode());

        return mapToOrderResponse(order);
    }

    @Override
    public void cancelOrder(Long orderId, String reason, String customerEmail) {
        Order order = null;
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            order = repository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found with id{}:" + orderId));
            order.setStatus(OrderSagaStatus.CANCELED);
            transactionManager.commit(status);
        }catch (Exception e) {
            log.error("Failed to updated order with status");
            throw new RuntimeException("Failed to update order");
        }
        // Publish cancellation event
        var canceledEvent = OrderCanceledEventDTO.newInstance(orderId, reason, customerEmail);

        producer.publishOrderCanceledEvent(canceledEvent, order.hashCode());

        log.info("Order {} canceled, notifying customer.", orderId);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        var order = findOrderById(orderId);
        return mapToOrderResponse(order);
    }

    private Order newInstance(OrderRequest request) {
        return Order.newInstance(
                request.userId(),
                request.totalAmount(),
                request.couponCode(),
                request.orderItems()
        );
    }

    private void saveOrderWithTransaction(Order order) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            repository.save(order);
            transactionManager.commit(status);
            log.info("Order saved locally with ID: {}", order.getOrderId());
        }catch (Exception e) {
            log.error("Failed to save order: {}", e.getMessage());
            transactionManager.rollback(status);
            throw new RuntimeException("Failed to create order", e);
        }
    }

    private void applyCouponWithTransaction(Order order) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            couponService.applyCoupon(order);
            transactionManager.commit(status);
            log.info("Order updated with discount for ID: {}", order.getOrderId());
        }catch (Exception e) {
            log.error("Failed to update order with discount", e);
            transactionManager.rollback(status);
            throw new RuntimeException("Failed to updated order");
        }
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return mapper.mapOrderToOrderResponse(order);
    }

    private Order findOrderById(Long orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found by id: " + orderId));
    }
}

