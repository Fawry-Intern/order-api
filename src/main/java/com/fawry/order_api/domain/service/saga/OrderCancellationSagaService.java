package com.fawry.order_api.domain.service.saga;

import java.util.concurrent.CompletableFuture;

public interface OrderCancellationSagaService {
     void cancelOrderSaga(Long orderId, String reason, String customerEmail);
}
