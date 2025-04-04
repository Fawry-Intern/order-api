package com.fawry.order_api.ports.outbound.coupon_service;

import com.fawry.order_api.dto.dtos.DiscountDTO;

import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.exception.CouponUnavailabilityException;
import com.fawry.order_api.exception.InvalidCouponCodeException;
import com.fawry.order_api.mapper.OrderMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDiscountServiceUseCase implements OrderDiscountService {

    private final CouponClient client;
    private final OrderMapper mapper;
    private static final String COUPON_SERVICE = "couponService";

    @Override
    @CircuitBreaker(name = COUPON_SERVICE, fallbackMethod = "applyCouponFallback")
    @Retry(name = COUPON_SERVICE)
    public DiscountDTO applyCoupon(Order order) {
        var couponRequest = mapper.mapOrderToConsumeCouponRequest(order);
        DiscountDTO discountDTO;
        try {
            ResponseEntity<DiscountDTO> response = client.consumeCoupon(couponRequest);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                discountDTO = response.getBody();
                log.info("Coupon {} applied successfully, discount: {}", order.getCouponCode(), discountDTO.getActualDiscount());
            } else {
                log.error("Invalid coupon response for order {}: Status {}", order.getOrderId(), response.getStatusCode());
                throw new InvalidCouponCodeException("Invalid coupon response from service");
            }
            return discountDTO;
        } catch (Exception e) {
            log.error("Failed to consume coupon {} for order {}: {}", order.getCouponCode(), order.getOrderId(), e.getMessage());
            throw new CouponUnavailabilityException("Coupon service unavailable or invalid coupon");
        }
    }

    public DiscountDTO applyCouponFallback(Order order, Throwable t) {
        log.warn("Coupon service unavailable for order {}. Applying zero discount. Error: {}",
                order.getOrderId(), t != null ? t.getMessage() : "Unknown error");
        throw new CouponUnavailabilityException("Coupon service unavailable or invalid coupon");
    }

}
