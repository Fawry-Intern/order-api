package com.fawry.order_api.domain.service;

import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.dto.dtos.OrderResponse;

public interface OrderCreationSagaService {
    OrderResponse createOrderSaga(OrderRequest request);
}
