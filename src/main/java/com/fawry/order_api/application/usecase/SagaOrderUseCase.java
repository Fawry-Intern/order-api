package com.fawry.order_api.application.usecase;

import com.fawry.kafka.events.OrderCancelNotificationEvent;
import com.fawry.kafka.events.OrderCreatedEventDTO;
import com.fawry.order_api.exception.CouponUnavailabilityException;
import com.fawry.order_api.infrastructure.messaging.producer.impl.OrderEventProducer;
import com.fawry.order_api.domain.service.OrderCancellationSagaService;
import com.fawry.order_api.domain.service.OrderCreationSagaService;
import com.fawry.order_api.dto.dtos.OrderItemDTO;
import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.dto.dtos.OrderResponse;
import com.fawry.order_api.domain.model.Money;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.domain.model.OrderItem;
import com.fawry.order_api.exception.ErrorMessages;
import com.fawry.order_api.exception.InvalidCouponCodeException;
import com.fawry.order_api.exception.OrderNotFoundException;
import com.fawry.order_api.mapper.OrderMapper;
import com.fawry.order_api.ports.outbound.coupon_service.OrderDiscountService;
import com.fawry.order_api.ports.outbound.auth.OrderUserAuth;
import com.fawry.order_api.infrastructure.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SagaOrderUseCase implements OrderCreationSagaService, OrderCancellationSagaService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderDiscountService couponService;
    private final PlatformTransactionManager transactionManager;
    private final OrderEventProducer<Object> producer;
    private final OrderUserAuth orderUserAuth;

    @Override
    public OrderResponse createOrderSaga(OrderRequest request) {
        Long userId = orderUserAuth.parseUserId();
        var order = newInstance(request, userId);
        saveOrderWithTransaction(order);

        if (!request.isCouponCodeValid()) {
            throw new InvalidCouponCodeException(ErrorMessages.INVALID_COUPON_CODE);
        }
        try {
            applyCouponWithTransaction(order);
        }catch (Exception e){
            log.error("Failed to apply coupon for order {}. Cancelling order.", order.getOrderId());
            cancelOrderSaga(order.getOrderId(), "Failed to apply coupon due to service unavailability", getCustomerEmail());
            throw new CouponUnavailabilityException("Failed to apply coupon, order  cancelled");
        }

        var orderCreatedEvent = mapToOrderCreatedEventDto(order, getCustomerEmail(), request);
        producer.processEventProducer(orderCreatedEvent, order.hashCode());

        return mapToOrderResponse(order);
    }

    @Override
    public void cancelOrderSaga(Long orderId, String reason, String customerEmail) {
        Order order;
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            order = findOrderById(orderId);
            order.cancel();
            transactionManager.commit(status);
        }catch (Exception e) {
            log.error("Failed to updated order with status");
            throw new RuntimeException("Failed to update order");
        }
        sendCancellationNotification(orderId, reason, customerEmail);
    }

    private void sendCancellationNotification(Long orderId, String reason, String customerEmail) {
        var canceledEvent = OrderCancelNotificationEvent.newInstance(orderId, reason, customerEmail, Instant.now());
        producer.processEventProducer(canceledEvent, orderId.hashCode());
        log.info("Order {} canceled. Notification sent to customer: {}", orderId, customerEmail);
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
        }
        catch (Exception e) {
            log.error("Failed to update order with discount", e);
            transactionManager.rollback(status);
            throw new RuntimeException("Failed to updated order");
        }
    }

    private Order newInstance(OrderRequest request, Long userId) {
        return Order.newInstance(
                userId,
                Money.of(request.totalAmount()),
                request.couponCode(),
                mapToOrderItem(request.orderItems())
        );
    }

    private Set<OrderItem> mapToOrderItem(List<OrderItemDTO> orderItems) {
        return mapper.mapToOrderItem(orderItems);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return mapper.mapOrderToOrderResponse(order);
    }

    private Order findOrderById(Long orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private OrderCreatedEventDTO mapToOrderCreatedEventDto(Order order, String customerEmail, OrderRequest request) {
        return mapper.mapFromOrderToOrderCreatedEvent(order,
                customerEmail,
                request.customerName(),
                request.customerContact(),
                request.addressDetails(),
                request.orderItems(),
                request.paymentMethod()
        );
    }

    public String getCustomerEmail() {
        return orderUserAuth.parseUserEmail();
    }
}

