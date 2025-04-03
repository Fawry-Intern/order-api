package com.fawry.order_api.application.service;
import com.fawry.order_api.dto.dtos.OrderResponse;

import java.time.Instant;
import java.util.List;

public interface OrderService {
    OrderResponse getOrderById(Long orderId);
     List<OrderResponse> searchOrdersByUserIdAndDateRange(Long userId, Instant startDate, Instant endDate, int page, int size);
}
