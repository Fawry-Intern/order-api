package com.fawry.order_api.dto.dtos;

import com.fawry.order_api.entities.OrderItem;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Set;

@Builder
public record OrderRequest(
        String customerName,
        String customerContact,
        AddressDetails addressDetails,
        BigDecimal totalAmount,
        String couponCode,
        Set<OrderItem> orderItems,
        PaymentMethod paymentMethod
) {
}
