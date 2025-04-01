package com.fawry.order_api.services;
public interface OrderCancellationSaga {
     void cancelOrder(Long orderId, String reason, String customerEmail);
}
