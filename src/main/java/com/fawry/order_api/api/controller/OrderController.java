package com.fawry.order_api.api.controller;

import com.fawry.order_api.domain.service.saga.OrderCreationSagaService;
import com.fawry.order_api.dto.dtos.OrderCreationJob;
import com.fawry.order_api.dto.dtos.OrderCreationResponse;
import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.application.service.OrderService;
import com.fawry.order_api.infrastructure.jobqueue.OrderJobProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("api/v1/orders/")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final OrderCreationSagaService orderCreationSaga;
    private final OrderJobProducer orderJobProducer;

    @PostMapping
    public CompletableFuture<ResponseEntity<OrderCreationJob>> createOrder(@Valid @RequestBody OrderRequest request) throws ExecutionException, InterruptedException {
        log.info("Thread name is {} ", Thread.currentThread().getName());
        return orderJobProducer.addJob(request)
                .thenApply((order) -> ResponseEntity.created(URI.create("orders")).body(order));
    }

    @GetMapping("/search-by-customer")
    public ResponseEntity<List<OrderCreationResponse>> searchOrdersByUserIdAndDateRange(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        List<OrderCreationResponse> orders = orderService.searchOrdersByUserIdAndDateRange(userId, startDate, endDate, page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{order-id}")
    public ResponseEntity<OrderCreationResponse> findOrderById(@PathVariable(name = "order-id") Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }
}
