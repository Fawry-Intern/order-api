package com.fawry.order_api.domain.service;

import com.fawry.order_api.dto.dtos.OrderCreationResponse;
import com.fawry.order_api.dto.dtos.OrderRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface OrderCreationSagaService {
    CompletableFuture<OrderCreationResponse> createOrderSaga(OrderRequest request) throws ExecutionException, InterruptedException;
}
