package com.fawry.order_api.ports.outbound.coupon_service;

import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.dto.dtos.DiscountDTO;

public interface OrderDiscountService {
    DiscountDTO applyCoupon(Order order);
}
