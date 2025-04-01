package com.fawry.order_api.ports.outbound.coupon_service;

import com.fawry.order_api.domain.model.Order;

public interface OrderDiscountService {
    void applyCoupon(Order order);
}
