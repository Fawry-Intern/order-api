package com.fawry.order_api.domain.event;

import com.fawry.order_api.dto.dtos.AddressDetails;
import com.fawry.order_api.dto.dtos.OrderItemDTO;
import com.fawry.order_api.dto.dtos.PaymentMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@RequiredArgsConstructor
@Getter
@ToString
public class OrderCreatedEventDTO implements Serializable {
    private final Long orderId;
    private final Long userId;
    private final String sagaEventType;
    private final String status;
    private final String customerEmail;
    private final String customerName;
    private final String customerContact;
    private final AddressDetails addressDetails;
    private final BigDecimal paymentAmount;
    private final List<OrderItemDTO> orderItems;
    private final PaymentMethod paymentMethod;

    public static OrderCreatedEventDTO newInstance(Long orderId,
                                                   Long userId,
                                                   String sagaEventType,
                                                   String status,
                                                   String customerEmail,
                                                   String customerName,
                                                   String customerContact,
                                                   AddressDetails addressDetails,
                                                   BigDecimal paymentAmount,
                                                   List<OrderItemDTO> orderItems,
                                                   PaymentMethod paymentMethod) {
        return new OrderCreatedEventDTO(orderId, userId, sagaEventType, status, customerEmail, customerName, customerContact, addressDetails, paymentAmount, orderItems, paymentMethod);
    }

}
