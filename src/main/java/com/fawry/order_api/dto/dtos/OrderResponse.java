package com.fawry.order_api.dto.dtos;

import com.fawry.order_api.domain.model.OrderItem;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Set;

@Builder
public record OrderResponse(
        Long orderId,
        BigDecimal totalAmount,
        String couponCode,
        Set<OrderItem> orderItem
) {
}
