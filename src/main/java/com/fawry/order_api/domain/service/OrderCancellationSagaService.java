package com.fawry.order_api.domain.service;

public interface OrderCancellationSagaService {
     void cancelOrderSaga(Long orderId, String reason, String customerEmail);
}
