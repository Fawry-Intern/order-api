package com.fawry.order_api.dto.dtos;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
class PaymentDetails {
    private String number;
    private String cvv;
    private String expiry;
}

