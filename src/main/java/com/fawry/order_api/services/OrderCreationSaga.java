package com.fawry.order_api.services;

import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.dto.dtos.OrderResponse;

public interface OrderCreationSaga {
    OrderResponse createOrder(OrderRequest request);
}
