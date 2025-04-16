package com.fawry.order_api.dto.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConsumeCouponRequestDTO {

    @NotBlank(message = "Coupon code must not be blank")
    private String couponCode;

    @NotNull(message = "Order Id must not be blank")
    private Long orderId;

    @Positive(message = "Value should be positive")
    private BigDecimal orderAmount;

    @NotNull(message = "User Id must not be blank")
    private Long userId;

}
