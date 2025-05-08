package com.fawry.order_api.api.controller;

import com.fawry.order_api.dto.dtos.OrderCreationJob;
import com.fawry.order_api.dto.dtos.OrderCreationResponse;
import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.application.service.OrderService;
import com.fawry.order_api.infrastructure.jobqueue.OrderJobProducer;
import com.fawry.order_api.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order API", description = "Endpoints for managing orders in the Order Service")
public class OrderController {

    private final OrderService orderService;
    private final OrderJobProducer orderJobProducer;
    private final OrderMapper orderMapper;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order by adding it to the job queue for asynchronous processing.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderCreationJob.class))),
            @ApiResponse(responseCode = "400", description = "Invalid order request data",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<OrderCreationJob> createOrder(
            @Valid @RequestBody OrderRequest request
    ) throws ExecutionException, InterruptedException {

        log.info("Thread name is {}", Thread.currentThread().getName());
        return ResponseEntity.created(URI.create("orders")).body(orderJobProducer.addJob(request).get());
    }


    @GetMapping("/search-by-customer")
    @Operation(summary = "Search orders by user ID and date range", description = "Retrieves a paginated list of orders for a specific user within a date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderCreationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<OrderCreationResponse>> searchOrdersByUserIdAndDateRange(
            @RequestParam @Parameter(description = "User ID to filter orders", required = true) Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "Start date of the range (ISO format)", required = true) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(description = "End date of the range (ISO format)", required = true) Instant endDate,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (default: 0)") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Page size (default: 10)") int size) {
        List<OrderCreationResponse> orders = orderService.searchOrdersByUserIdAndDateRange(userId, startDate, endDate, page, size);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{order-id}")
    @Operation(summary = "Get order by ID", description = "Retrieves the details of a specific order by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderCreationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<OrderCreationResponse> findOrderById(
            @PathVariable(name = "order-id") @Parameter(description = "ID of the order to retrieve", required = true) Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }
}
