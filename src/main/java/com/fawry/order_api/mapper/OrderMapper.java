package com.fawry.order_api.mapper;

import com.fawry.order_api.dto.dtos.*;
import com.fawry.order_api.dto.enums.SagaEventType;
import com.fawry.kafka.events.OrderCreatedEventDTO;
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

    public OrderCreationJob mapToOrderCreationJob(OrderRequest request) {
        return OrderCreationJob.builder()
                .orderItem(request.orderItems())
                .status(OrderSagaStatus.PENDING)
                .couponCode(request.couponCode())
                .totalAmount(request.totalAmount())
                .build();
    }
    public Set<OrderItem> mapToOrderItem(List<OrderItemDTO> orderItems) {
        return orderItems.stream().
                map((orderItemDTO -> OrderItem.newInstance(orderItemDTO.getStoreId(), orderItemDTO.getProductId(), orderItemDTO.getQuantity(), Money.of(orderItemDTO.getPrice())))).collect(Collectors.toSet());
    }

    public OrderCreationResponse mapOrderToOrderResponse(Order order) {
        return OrderCreationResponse.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
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
                OrderSagaStatus.CREATED.name(),
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
