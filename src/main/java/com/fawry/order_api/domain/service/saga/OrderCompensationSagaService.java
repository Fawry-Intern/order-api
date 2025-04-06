package com.fawry.order_api.domain.service.saga;

public interface OrderCompensationSagaService {
    void compensateOrder(Long orderId, String reason, String customerEmail);
}
