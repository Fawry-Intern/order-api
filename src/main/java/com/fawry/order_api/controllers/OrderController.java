package com.fawry.order_api.controllers;

import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.dto.dtos.OrderResponse;
import com.fawry.order_api.services.OrderService;
import com.fawry.order_api.services.OrderCreationSaga;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderCreationSaga orderCreationSaga;


    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderCreationSaga.createOrder(request));
    }

    @GetMapping("/{order-id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable(value = "order-id") Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }
}
