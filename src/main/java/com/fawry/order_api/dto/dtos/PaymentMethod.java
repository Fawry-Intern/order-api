package com.fawry.order_api.dto.dtos;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record PaymentMethod(
        PaymentDetails details
) implements Serializable {

    @Override
    public PaymentDetails details() {
        return details;
    }

    @Override
    public String toString() {
        return "" + details;
    }
}
