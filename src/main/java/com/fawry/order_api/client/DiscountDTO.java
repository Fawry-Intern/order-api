package com.fawry.order_api.client;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Builder
@ToString
public class DiscountDTO {
    private BigDecimal actualDiscount;
}
