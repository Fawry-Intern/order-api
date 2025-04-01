package com.fawry.order_api.dto.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
@ToString
public class OrderItemDTO {
    private final Long storeId;
    private final Long productId;
    private final Integer quantity;
    private final BigDecimal price;
}
