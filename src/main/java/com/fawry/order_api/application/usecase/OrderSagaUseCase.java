package com.fawry.order_api.application.usecase;

import com.fawry.order_api.domain.event.OrderCancelNotificationEvent;
import com.fawry.order_api.domain.model.Outbox;
import com.fawry.order_api.domain.service.saga.OrderCompensationSagaService;
import com.fawry.order_api.dto.dtos.DiscountDTO;
import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.dto.enums.OrderSagaStatus;
import com.fawry.order_api.exception.*;
import com.fawry.order_api.infrastructure.messaging.producer.impl.OrderEventProducer;
import com.fawry.order_api.domain.service.saga.OrderCancellationSagaService;
import com.fawry.order_api.domain.service.saga.OrderCreationSagaService;
import com.fawry.order_api.dto.dtos.OrderItemDTO;
import com.fawry.order_api.domain.model.Money;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.domain.model.OrderItem;
import com.fawry.order_api.infrastructure.repository.OutboxRepository;
import com.fawry.order_api.infrastructure.transaction.TransactionExecutor;
import com.fawry.order_api.mapper.OrderMapper;
import com.fawry.order_api.mapper.OutboxMapper;
import com.fawry.order_api.ports.outbound.coupon_service.OrderDiscountService;
import com.fawry.order_api.infrastructure.repository.OrderRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaUseCase implements OrderCreationSagaService, OrderCancellationSagaService, OrderCompensationSagaService {

    private final OutboxRepository outboxRepository;
    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderDiscountService couponService;
    private final TransactionExecutor transactionExecutor;
    private final OrderEventProducer<Object> producer;
    private final OutboxMapper outboxMapper;


    @Override
    @Async("orderTaskExecutor")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CompletableFuture<Void> createOrderSaga(OrderRequest request) {
        Order order = createAndSaveOrder(request);

        try {
            checkCouponValidate(request);
            var discountDto = applyCouponToOrder(order);
            updateOrderWithCouponDiscountTransaction(order, discountDto);
        }catch (Exception e) {
            log.error("Saga failed: {}", e.getMessage());
            compensateFailedStartingSaga(order, e, order.getUserEmail());
            throw new OrderProcessingException("Failed to proccessing order", e.getMessage());
        }

        saveOrderEventToDatabase(request, order, OrderSagaStatus.CREATED, order.getUserEmail());
        return CompletableFuture.completedFuture(null);
    }

    private Order createAndSaveOrder(   OrderRequest request) {
        var order = newInstance(request);
        log.info("Saving order for userId: {} in thread: {}", request.customerId(), Thread.currentThread().getName());
        saveOrderInDatabase(order);
        log.info("Order saved successfully with orderId: {}", order.getOrderId());
        return order;
    }

    private void saveOrderInDatabase(Order order) {
        try {
            repository.save(order);
        } catch (Exception e) {
            throw new OrderProcessingException("Error processing order creation", e.getMessage());
        }
    }

    private Order newInstance(OrderRequest request) {
        return Order.newInstance(request.customerId(),request.customerEmail(), Money.of(request.totalAmount())
                , request.couponCode(), mapToOrderItem(request.orderItems())
        );
    }

    private void saveOrderEventToDatabase(OrderRequest request, Order order, OrderSagaStatus status, String customerEmail) {
        Outbox outbox = new Outbox();
        try {
            outbox = outboxMapper.mapToOutbox(request, order, outbox);
            outboxRepository.save(outbox);
        }catch (Exception e) {
            throw new OrderProcessingException("Error processing order event creation", e.getMessage());
        }
    }

    private void compensateFailedStartingSaga(Order order, Exception e, String customerEmail) {
        if (order != null && order.getOrderId() != null) {
            try {
                cancelOrderSaga(order.getOrderId(), e.getMessage(), customerEmail);
            }catch (Exception exception) {
                log.error("Compensation failed: {}", exception.getMessage());
            }
        }
    }

    private void checkCouponValidate(OrderRequest request) {
        if (!request.isCouponCodeValid()) {
            throw new InvalidCouponCodeException(ErrorMessages.INVALID_COUPON_CODE);
        }
    }

    private DiscountDTO applyCouponToOrder(Order order) {
        try {
            return applyCouponWithTransaction(order);
        } catch (Exception e) {
            log.error("Failed to apply coupon for orderId: {}. Error: {}", order.getOrderId(), e.getMessage(), e);
            throw new CouponUnavailabilityException("Failed to apply coupon: " + e.getMessage());
        }
    }

    private DiscountDTO applyCouponWithTransaction(Order order) {
        return couponService.applyCoupon(order);
    }

    private void updateOrderWithCouponDiscountTransaction(Order order, DiscountDTO discountDTO) {
        transactionExecutor.executeInTransaction(() -> {
            Money discount = Money.of(discountDTO.getActualDiscount());
            order.applyDiscount(discount);
            return repository.save(order);
        });
    }

    private Set<OrderItem> mapToOrderItem(List<OrderItemDTO> orderItems) {
        return mapper.mapToOrderItem(orderItems);
    }

    @Override
    @Async("orderTaskExecutor")
    public void cancelOrderSaga(Long orderId, String reason, String customerEmail) {

            transactionExecutor.executeInTransaction(() -> {
                Order order = findOrderById(orderId);
                order.cancel();
                return order;
            });

            try {
                sendCancellationNotification(orderId, reason, customerEmail);
            } catch (Exception e) {
                throw new RuntimeException("Failed to send cancellation notification for orderId: " + orderId, e);
            }

    }

    @Override
    public void compensateOrder(Long orderId, String reason, String customerEmail) {
        if (orderId != null && StringUtils.isNotBlank(reason) && StringUtils.isNotBlank(customerEmail)) {
            try {
                cancelOrderSaga(orderId, reason, customerEmail);
            }catch (Exception exception) {
                log.error("Compensation failed: {}", exception.getMessage());
            }
        }
    }

    private void sendCancellationNotification(Long orderId, String reason, String customerEmail) {
        var canceledEvent = OrderCancelNotificationEvent.newInstance(orderId, reason, customerEmail, Instant.now());
        producer.processEventProducer(canceledEvent, orderId.hashCode());
    }

    private Order findOrderById(Long orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

}

