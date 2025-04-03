package com.fawry.order_api.aspect;


import com.fawry.order_api.dto.dtos.OrderResponse;
import com.fawry.order_api.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;

@Aspect
@Component
@Slf4j
public class OrderRetrievalAspect {

    @Around("execution(* com.fawry.order_api.application.usecase.OrderSearchUseCase.getOrderById(..))")
    public Object logAndMonitorGetOrderById(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length != 1) {
            log.error("Invalid input for getOrderById: Expected 1 argument, but got {}", args.length);
            throw new IllegalArgumentException("Invalid input: getOrderById requires orderId");
        }

        Long orderId = (Long) args[0];
        if (orderId == null || orderId <= 0) {
            log.error("Invalid orderId for getOrderById: {}", orderId);
            throw new IllegalArgumentException("Invalid orderId: must be a positive number");
        }

        long startTime = System.currentTimeMillis();
        log.info("Starting getOrderById for orderId: {}", orderId);

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("getOrderById completed successfully for orderId: {} in {} ms. Response: {}", orderId, executionTime, result);

            return result;

        } catch (OrderNotFoundException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Order not found for orderId: {} after {} ms. Error: {}", orderId, executionTime, e.getMessage());
            throw e;

        } catch (Throwable t) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Unexpected error in getOrderById for orderId: {} after {} ms. Error: {}", orderId, executionTime, t.getMessage(), t);
            throw new RuntimeException("Unexpected error in getOrderById", t);
        }
    }

    @Around("execution(* com.fawry.order_api.application.usecase.OrderSearchUseCase.searchOrdersByUserIdAndDateRange(..))")
    public Object logAndMonitorSearchOrders(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length != 5) {
            log.error("Invalid input for searchOrdersByUserIdAndDateRange: Expected 5 arguments, but got {}", args.length);
            throw new IllegalArgumentException("Invalid input: searchOrdersByUserIdAndDateRange requires userId, startDate, endDate, page, and size");
        }

        Long userId = (Long) args[0];
        Instant startDate = (Instant) args[1];
        Instant endDate = (Instant) args[2];
        Integer page = (Integer) args[3];
        Integer size = (Integer) args[4];

        if (userId == null || userId <= 0) {
            log.error("Invalid userId for searchOrdersByUserIdAndDateRange: {}", userId);
            throw new IllegalArgumentException("Invalid userId: must be a positive number");
        }
        if (startDate == null || endDate == null) {
            log.error("Invalid date range for searchOrdersByUserIdAndDateRange: startDate={}, endDate={}", startDate, endDate);
            throw new IllegalArgumentException("Invalid date range: startDate and endDate must not be null");
        }
        if (startDate.isAfter(endDate)) {
            log.error("Invalid date range for searchOrdersByUserIdAndDateRange: startDate={} is after endDate={}", startDate, endDate);
            throw new IllegalArgumentException("Invalid date range: startDate must be before endDate");
        }
        if (page < 0) {
            log.error("Invalid page for searchOrdersByUserIdAndDateRange: {}", page);
            throw new IllegalArgumentException("Invalid page: must be non-negative");
        }
        if (size <= 0) {
            log.error("Invalid size for searchOrdersByUserIdAndDateRange: {}", size);
            throw new IllegalArgumentException("Invalid size: must be a positive number");
        }

        long startTime = System.currentTimeMillis();
        log.info("Starting searchOrdersByUserIdAndDateRange for userId: {}, startDate: {}, endDate: {}, page: {}, size: {}",
                userId, startDate, endDate, page, size);

        try {
            Object result = joinPoint.proceed();
            List<OrderResponse> orderResponses = (List<OrderResponse>) result;

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("searchOrdersByUserIdAndDateRange completed successfully for userId: {} in {} ms. Found {} orders",
                    userId, executionTime, orderResponses.size());

            return result;

        } catch (Throwable t) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Unexpected error in searchOrdersByUserIdAndDateRange for userId: {} after {} ms. Error: {}",
                    userId, executionTime, t.getMessage(), t);
            throw new RuntimeException("Unexpected error in searchOrdersByUserIdAndDateRange", t);
        }
    }
}