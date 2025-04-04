package com.fawry.order_api.application.usecase;

import com.fawry.order_api.dto.dtos.OrderCreationResponse;
import com.fawry.order_api.exception.OrderNotFoundException;
import com.fawry.order_api.mapper.OrderMapper;
import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.infrastructure.repository.OrderRepository;
import com.fawry.order_api.application.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSearchUseCase implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final Executor orderTaskExecutor;

    @Override
    public OrderCreationResponse getOrderById(Long orderId) {
        var order= orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return orderMapper.mapOrderToOrderResponse(order);
    }

    @Override
    public List<OrderCreationResponse> searchOrdersByUserIdAndDateRange(Long userId, Instant startDate, Instant endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> ordersPage = orderRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable);
        return ordersPage.stream()
                .map(orderMapper::mapOrderToOrderResponse)
                .collect(Collectors.toList());
    }

}
