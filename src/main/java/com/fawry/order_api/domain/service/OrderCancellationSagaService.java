package com.fawry.order_api.domain.service;

public interface OrderCancellationSagaService {
     void cancelOrder(Long orderId, String reason, String customerEmail);
}
