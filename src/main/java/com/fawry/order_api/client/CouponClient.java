package com.fawry.order_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "COUPON-API", path = "/api/coupons")
public interface CouponClient {

    @PostMapping("/consume")
    ResponseEntity<DiscountDTO> consumeCoupon(@RequestBody ConsumeCouponRequestDTO requestDTO);
}
