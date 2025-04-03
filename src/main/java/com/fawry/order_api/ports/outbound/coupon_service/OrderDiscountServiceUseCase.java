package com.fawry.order_api.ports.outbound.coupon_service;

import com.fawry.order_api.dto.dtos.DiscountDTO;
import com.fawry.order_api.domain.model.Money;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.mapper.OrderMapper;
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

    @Override
    public void applyCoupon(Order order) {
        var couponRequest = mapper.mapOrderToConsumeCouponRequest(order);

        try {
            ResponseEntity<DiscountDTO> response = client.consumeCoupon(couponRequest);

            log.info("Order total amount before discount{}:", order.getPaymentAmount().getAmount());
            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DiscountDTO discountDTO = response.getBody();
                applyDiscount(order, discountDTO);
                log.info("Coupon {} applied successfully, discount: {}", order.getCouponCode(), discountDTO.getActualDiscount());
            }
            log.info("Order total amount after discount{}:", order.getPaymentAmount().getAmount());
        }catch (Exception e) {
            log.error("Failed to consume coupon {} for order {}: {}", order.getCouponCode(), order.getOrderId(), e.getMessage());
            throw new RuntimeException("Coupon service unavailable or invalid coupon", e);
        }
    }

    private void applyDiscount(Order order, DiscountDTO discountDTO) {
        Money discount = Money.of(discountDTO.getActualDiscount());
        order.applyDiscount(discount);
    }
}
