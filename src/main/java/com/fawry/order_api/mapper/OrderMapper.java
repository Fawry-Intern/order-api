package com.fawry.order_api.mapper;

import com.fawry.kafka.dto.enums.SagaEventType;
import com.fawry.kafka.events.OrderCreatedEventDTO;
import com.fawry.order_api.client.ConsumeCouponRequestDTO;
import com.fawry.order_api.dto.dtos.AddressDetails;
import com.fawry.order_api.dto.dtos.OrderResponse;
import com.fawry.order_api.dto.dtos.PaymentMethod;
import com.fawry.order_api.dto.enums.OrderSagaStatus;
import com.fawry.order_api.entities.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {

    public OrderResponse mapOrderToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .orderItem(order.getOrderItems())
                .build();
    }

    public ConsumeCouponRequestDTO mapOrderToConsumeCouponRequest(Order order) {
        return ConsumeCouponRequestDTO
                .builder()
                .orderId(order.getOrderId())
                .orderAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .build();
    }

    public OrderCreatedEventDTO mapFromOrderToOrderCreatedEvent(Order order,
                                                                String customerEmail,
                                                                String customerName,
                                                                String customerContact,
                                                                AddressDetails addressDetails,
                                                                PaymentMethod paymentMethod
    ) {
        return new OrderCreatedEventDTO(
                order.getOrderId(),
                order.getUserId(),
                SagaEventType.ORDER_CREATED,
                OrderSagaStatus.RECEIVED,
                customerEmail,
                customerName,
                customerContact,
                addressDetails,
                order.getTotalAmount(),
                order.getOrderItems(),
                paymentMethod

        );
    }
}
