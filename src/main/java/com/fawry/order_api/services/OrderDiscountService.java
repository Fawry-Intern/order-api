package com.fawry.order_api.services;

import com.fawry.order_api.entities.Order;

public interface OrderDiscountService {
    void applyCoupon(Order order);
}
