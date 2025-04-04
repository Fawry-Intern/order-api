package com.fawry.order_api.domain.service;

import java.util.concurrent.CompletableFuture;

public interface OrderCancellationSagaService {
     CompletableFuture<Void> cancelOrderSaga(Long orderId, String reason, String customerEmail);
}
