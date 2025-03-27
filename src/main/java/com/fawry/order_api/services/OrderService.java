package com.fawry.order_api.services;
import com.fawry.order_api.dto.dtos.OrderResponse;

public interface OrderService {
    OrderResponse getOrderById(Long orderId);
}
