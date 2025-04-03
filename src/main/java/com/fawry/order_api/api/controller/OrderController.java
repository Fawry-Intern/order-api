package com.fawry.order_api.api.controller;

import com.fawry.order_api.domain.service.OrderCreationSagaService;
import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.dto.dtos.OrderResponse;
import com.fawry.order_api.application.service.OrderService;
import com.fawry.order_api.application.usecase.OrderSearchUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("api/v1/orders/")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderCreationSagaService orderCreationSaga;
    private final OrderSearchUseCase orderSearchUseCase;


    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderCreationSaga.createOrderSaga(request));
    }

    @GetMapping("/search-by-customer")
    public ResponseEntity<List<OrderResponse>> searchOrdersByUserIdAndDateRange(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        List<OrderResponse> orders = orderSearchUseCase.searchOrdersByUserIdAndDateRange(userId, startDate, endDate, page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{order-id}")
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable(name = "order-id") Long orderId) {
        return ResponseEntity.ok(orderSearchUseCase.getOrderById(orderId));
    }
}
