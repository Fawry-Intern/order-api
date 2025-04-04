package com.fawry.order_api.application.service;
import com.fawry.order_api.dto.dtos.OrderCreationResponse;

import java.time.Instant;
import java.util.List;

public interface OrderService {
    OrderCreationResponse getOrderById(Long orderId);
    List<OrderCreationResponse> searchOrdersByUserIdAndDateRange(Long userId, Instant startDate, Instant endDate, int page, int size);
}
