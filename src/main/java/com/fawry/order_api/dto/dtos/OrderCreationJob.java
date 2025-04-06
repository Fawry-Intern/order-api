package com.fawry.order_api.dto.dtos;

import com.fawry.order_api.dto.enums.OrderSagaStatus;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Builder(toBuilder = true)
public record OrderCreationJob(
        OrderSagaStatus status,
        BigDecimal totalAmount,
        String couponCode,
        List<OrderItemDTO> orderItem
) {

}
