package com.fawry.order_api.mapper;

import com.fawry.order_api.dto.enums.SagaEventType;
import com.fawry.kafka.events.OrderCreatedEventDTO;
import com.fawry.order_api.dto.dtos.ConsumeCouponRequestDTO;
import com.fawry.order_api.dto.dtos.AddressDetails;
import com.fawry.order_api.dto.dtos.OrderItemDTO;
import com.fawry.order_api.dto.dtos.OrderResponse;
import com.fawry.order_api.dto.dtos.PaymentMethod;
import com.fawry.order_api.dto.enums.OrderSagaStatus;
import com.fawry.order_api.domain.model.Money;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.domain.model.OrderItem;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class OrderMapper {

    public Set<OrderItem> mapToOrderItem(List<OrderItemDTO> orderItems) {
        return orderItems.stream().
                map((orderItemDTO -> {
                    return OrderItem.newInstance(orderItemDTO.getProductId(), orderItemDTO.getQuantity(), Money.of(orderItemDTO.getPrice()));
                })).collect(Collectors.toSet());
    }

    public OrderResponse mapOrderToOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .totalAmount(order.getPaymentAmount().getAmount())
                .couponCode(order.getCouponCode())
                .orderItem(order.getOrderItems())
                .build();
    }

    public ConsumeCouponRequestDTO mapOrderToConsumeCouponRequest(Order order) {
        return ConsumeCouponRequestDTO
                .builder()
                .orderId(order.getOrderId())
                .orderAmount(order.getPaymentAmount().getAmount())
                .couponCode(order.getCouponCode())
                .build();
    }

    public OrderCreatedEventDTO mapFromOrderToOrderCreatedEvent(Order order,
                                                                String customerEmail,
                                                                String customerName,
                                                                String customerContact,
                                                                AddressDetails addressDetails,
                                                                List<OrderItemDTO> orderItems,
                                                                PaymentMethod paymentMethod) {
        return OrderCreatedEventDTO.newInstance(
                order.getOrderId(),
                order.getUserId(),
                SagaEventType.ORDER_CREATED.name(),
                OrderSagaStatus.RECEIVED.name(),
                customerEmail,
                customerName,
                customerContact,
                addressDetails,
                order.getPaymentAmount().getAmount(),
                orderItems,
                paymentMethod

        );
    }


}
