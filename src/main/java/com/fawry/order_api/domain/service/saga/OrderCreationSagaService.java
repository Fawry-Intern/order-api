package com.fawry.order_api.domain.service.saga;

import com.fawry.order_api.dto.dtos.OrderRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface OrderCreationSagaService {
    CompletableFuture<Void> createOrderSaga(OrderRequest request) throws ExecutionException, InterruptedException;
}
