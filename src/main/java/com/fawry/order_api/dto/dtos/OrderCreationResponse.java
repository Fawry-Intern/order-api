package com.fawry.order_api.dto.dtos;

import com.fawry.order_api.domain.model.OrderItem;
import com.fawry.order_api.dto.enums.OrderSagaStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Set;

@Builder
public record OrderCreationResponse(
        Long orderId,
        OrderSagaStatus status,
        BigDecimal totalAmount,
        String couponCode,
        Set<OrderItem> orderItem
) {
}
