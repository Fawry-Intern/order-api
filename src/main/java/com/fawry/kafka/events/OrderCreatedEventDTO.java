package com.fawry.kafka.events;

import com.fawry.kafka.dto.enums.SagaEventType;
import com.fawry.order_api.dto.dtos.AddressDetails;
import com.fawry.order_api.dto.dtos.PaymentMethod;
import com.fawry.order_api.dto.enums.OrderSagaStatus;
import com.fawry.order_api.entities.OrderItem;


import java.math.BigDecimal;
import java.util.Set;

public class OrderCreatedEventDTO extends OrderSagaEvent {
    private final Long userId;
    private final SagaEventType sagaEventType;
    private final OrderSagaStatus status;
    private final String customerEmail;
    private final String customerName;
    private final String customerContact;
    private final AddressDetails addressDetails;
    private final BigDecimal paymentAmount;
    private final Set<OrderItem> orderItems;
    private final PaymentMethod paymentMethod;

    public OrderCreatedEventDTO(Long orderId,
                                Long userId,
                                SagaEventType sagaEventType,
                                OrderSagaStatus status,
                                String customerEmail,
                                String customerName,
                                String customerContact,
                                AddressDetails addressDetails,
                                BigDecimal paymentAmount,
                                Set<OrderItem> orderItems,
                                PaymentMethod paymentMethod) {
        super(orderId);
        this.userId = userId;
        this.sagaEventType = sagaEventType;
        this.status = status;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.customerContact = customerContact;
        this.addressDetails = addressDetails;
        this.paymentAmount = paymentAmount;
        this.orderItems = orderItems;
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return "OrderCreatedSagaEvent{" +
                "userId=" + userId +
                ", sagaEventType=" + sagaEventType +
                ", status=" + status +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerContact='" + customerContact + '\'' +
                ", addressDetails=" + addressDetails +
                ", paymentAmount=" + paymentAmount +
                ", orderItems=" + orderItems +
                ", paymentMethod=" + paymentMethod +
                '}';
    }
}
