package com.fawry.order_api.aspect;

import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.exception.CouponUnavailabilityException;
import com.fawry.order_api.exception.InvalidCouponCodeException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class OrderSagaAspect {

    private final MeterRegistry meterRegistry;
    private final Timer orderCreationTimer;

    public OrderSagaAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.orderCreationTimer = Timer.builder("order.creation.duration")
                .description("Time taken to create an order in SagaOrderUseCase")
                .register(meterRegistry);
    }

    @Around("execution(* com.fawry.order_api.application.usecase.SagaOrderUseCase.createOrderSaga(..))")
    public Object logAndMonitorOrderCreation(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();
        if (args.length == 0 || !(args[0] instanceof OrderRequest request)){
            log.error("Invalid input for createOrderSaga: Expected OrderRequest, but got {}", args);
            throw new IllegalArgumentException("Invalid input: OrderRequest is required");
        }

        if (ObjectUtils.equals(request, null)) {
            log.error("OrderRequest is null in createOrderSaga");
            throw new IllegalArgumentException("OrderRequest cannot be null");
        }

        log.info("Starting createOrderSaga with request: {}", request);
        return orderCreationTimer.record(() -> {
            try {
                Object result = joinPoint.proceed();
                log.info("createOrderSaga completed successfully. Response: {}", result);
                return result;

            } catch (InvalidCouponCodeException e) {
                log.error("Invalid coupon code in createOrderSaga. Error: {}", e.getMessage());
                throw e;

            } catch (CouponUnavailabilityException e) {
                log.error("Coupon service unavailable in createOrderSaga. Error: {}", e.getMessage());
                throw e;

            } catch (Throwable t) {
                log.error("Unexpected error in createOrderSaga. Error: {}", t.getMessage(), t);
                throw new RuntimeException("Unexpected error in createOrderSaga", t);
            }
        });
    }

    @Around("execution(* com.fawry.order_api.application.usecase.SagaOrderUseCase.cancelOrderSaga(..))")
    public Object logAndMonitorCancelOrder(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object[] args = joinPoint.getArgs();
        Long orderId = (Long) args[0];
        String reason = (String) args[1];
        String customerEmail = (String) args[2];
        log.info("Starting cancelOrderSaga for orderId: {}, reason: {}, customerEmail: {}", orderId, reason, customerEmail);

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("cancelOrderSaga completed successfully for orderId: {} in {} ms", orderId, executionTime);

            return result;

        } catch (RuntimeException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Failed to cancel order with orderId: {} after {} ms. Error: {}", orderId, executionTime, e.getMessage());
            throw e;

        } catch (Throwable t) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Unexpected error in cancelOrderSaga for orderId: {} after {} ms. Error: {}", orderId, executionTime, t.getMessage(), t);
            throw new RuntimeException("Unexpected error in cancelOrderSaga", t);
        }
    }
}
