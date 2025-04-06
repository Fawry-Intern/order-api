package com.fawry.order_api.application.usecase;

import com.fawry.kafka.events.OrderCancelNotificationEvent;
import com.fawry.kafka.events.OrderCreatedEventDTO;
import com.fawry.order_api.domain.service.saga.OrderCompensationSagaService;
import com.fawry.order_api.dto.dtos.DiscountDTO;
import com.fawry.order_api.dto.dtos.OrderCreationResponse;
import com.fawry.order_api.exception.CouponUnavailabilityException;
import com.fawry.order_api.infrastructure.messaging.producer.impl.OrderEventProducer;
import com.fawry.order_api.domain.service.saga.OrderCancellationSagaService;
import com.fawry.order_api.domain.service.saga.OrderCreationSagaService;
import com.fawry.order_api.dto.dtos.OrderItemDTO;
import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.domain.model.Money;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.domain.model.OrderItem;
import com.fawry.order_api.exception.ErrorMessages;
import com.fawry.order_api.exception.InvalidCouponCodeException;
import com.fawry.order_api.exception.OrderNotFoundException;
import com.fawry.order_api.infrastructure.transaction.TransactionExecutor;
import com.fawry.order_api.mapper.OrderMapper;
import com.fawry.order_api.ports.outbound.coupon_service.OrderDiscountService;
import com.fawry.order_api.ports.outbound.auth.OrderUserAuth;
import com.fawry.order_api.infrastructure.repository.OrderRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaUseCase implements OrderCreationSagaService, OrderCancellationSagaService, OrderCompensationSagaService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final OrderDiscountService couponService;
    private final TransactionExecutor transactionExecutor;
    private final OrderEventProducer<Object> producer;
    private final OrderUserAuth orderUserAuth;
    private final Executor orderTaskExecutor;
    private final static int MAX_RETRIES = 3;


    @Override
    @Async("orderTaskExecutor")
    public CompletableFuture<OrderCreationResponse> createOrderSaga(OrderRequest request) {
        Order order = null;
        try {
            log.info("Thread name is {} ", Thread.currentThread().getName());
            Long userId = orderUserAuth.parseUserId();
            order = createAndSaveOrder(request, userId);
            checkCouponValidate(request);
            var discountDto = applyCouponToOrder(order);
            updateOrderWithCouponDiscountTransaction(order, discountDto);
            publishOrderCreatedEvent(order, request);
            return CompletableFuture.completedFuture(mapToOrderResponse(order));
        }catch (Exception e) {
            log.error("Saga failed: {}", e.getMessage());
            compensateFailedStartingSaga(order, e, getCustomerEmail());
            throw e;
        }
    }

    @Override
    @Async("orderTaskExecutor")
    public CompletableFuture<Void> cancelOrderSaga(Long orderId, String reason, String customerEmail) {
        CompletableFuture<Void> updateOrderFuture = CompletableFuture.runAsync(() -> {
            transactionExecutor.executeInTransaction(() -> {
                Order order = findOrderById(orderId);
                order.cancel();
                return order;
            });
        });

        CompletableFuture<Void> sendNotificationFuture = updateOrderFuture.thenRunAsync(() -> {
            try {
                sendCancellationNotification(orderId, reason, customerEmail);
            } catch (Exception e) {
                throw new RuntimeException("Failed to send cancellation notification for orderId: " + orderId, e);
            }
        }, orderTaskExecutor);

        return CompletableFuture.allOf(updateOrderFuture, sendNotificationFuture)
                .handle((result, ex) -> {
                    if (ex != null) {
                        throw new RuntimeException("Cancellation saga failed for orderId: " + orderId, ex.getCause());
                    }
                    return null;
                });
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

    private void compensateFailedStartingSaga(Order order, Exception e, String customerEmail) {
        if (order != null && order.getOrderId() != null) {
            try {
                cancelOrderSaga(order.getOrderId(), e.getMessage(), customerEmail);
            }catch (Exception exception) {
                log.error("Compensation failed: {}", exception.getMessage());
            }
        }
    }

    private Order createAndSaveOrder(OrderRequest request, Long userId) {
        var order = newInstance(request, userId);
        log.info("Saving order for userId: {} in thread: {}", userId, Thread.currentThread().getName());
        saveOrderWithTransaction(order);
        log.info("Order saved successfully with orderId: {}", order.getOrderId());
        return order;
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
    private void publishOrderCreatedEvent(Order order, OrderRequest request) {
        var orderCreatedEvent = mapToOrderCreatedEventDto(order, getCustomerEmail(), request);
        producer.processEventProducer(orderCreatedEvent, order.hashCode());
    }

    private void sendCancellationNotification(Long orderId, String reason, String customerEmail) {
        var canceledEvent = OrderCancelNotificationEvent.newInstance(orderId, reason, customerEmail, Instant.now());
        producer.processEventProducer(canceledEvent, orderId.hashCode());
    }

    private void saveOrderWithTransaction(Order order) {

        transactionExecutor.executeInTransaction(() -> {
            log.info("Starting transaction to save order in db");
            return repository.save(order);
        });
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

    private OrderCreationResponse mapToOrderResponse(Order order) {
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

