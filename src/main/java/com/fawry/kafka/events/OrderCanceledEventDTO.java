package com.fawry.kafka.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


import java.io.Serializable;

@Getter
public class OrderCanceledEventDTO implements Serializable {

    private final Long orderId;
    private final String reason;
    private final String customerEmail;

    @JsonCreator
    public OrderCanceledEventDTO(@JsonProperty("orderId") Long orderId,
                                 @JsonProperty("reason") String reason,
                                 @JsonProperty("customerEmail") String customerEmail) {
        this.orderId = orderId;
        this.reason = reason;
        this.customerEmail = customerEmail;
    }

    public static OrderCanceledEventDTO newInstance(Long orderId, String reason, String customerEmail) {
        return new OrderCanceledEventDTO(orderId, reason, customerEmail);
    }

}
