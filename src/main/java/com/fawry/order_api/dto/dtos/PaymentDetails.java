package com.fawry.order_api.dto.dtos;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class PaymentDetails implements Serializable {
    private String number;
    private String cvv;
    private String expiry;
}

