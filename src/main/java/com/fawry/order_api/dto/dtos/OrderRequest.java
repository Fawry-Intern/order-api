package com.fawry.order_api.dto.dtos;


import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;


@Builder(toBuilder = true)
public record OrderRequest(
        @NotBlank(message = "Customer name must not be blank")
        String customerName,

        @NotBlank(message = "Customer contact must not be blank")
        String customerContact,

        @NotNull(message = "Address details must not be null")
        @Valid
        AddressDetails addressDetails,

        @NotNull(message = "Total amount must not be null")
        @DecimalMin(value = "0.00", inclusive = false, message = "Total amount must be greater than zero")
        BigDecimal totalAmount,

        String couponCode,

        @NotEmpty(message = "Order items must not be empty")
        @Valid
        List<OrderItemDTO> orderItems,

        @NotNull(message = "Payment method must not be null")
        PaymentMethod paymentMethod
) {

    public boolean isCouponCodeValid() {
        return couponCode != null && !couponCode.isBlank();
    }
}
