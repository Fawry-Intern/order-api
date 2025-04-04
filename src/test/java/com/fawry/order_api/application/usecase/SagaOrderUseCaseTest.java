package com.fawry.order_api.application.usecase;

import com.fawry.order_api.domain.model.Order;
import com.fawry.order_api.infrastructure.repository.OrderRepository;
import com.fawry.order_api.mapper.OrderMapper;
import com.fawry.order_api.ports.outbound.auth.OrderUserAuth;
import com.fawry.order_api.ports.outbound.coupon_service.OrderDiscountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SagaOrderUseCaseTest {
    @InjectMocks
    private SagaOrderUseCase sagaOrderUseCase;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderDiscountService orderDiscountService;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private OrderUserAuth orderUserAuth;


    @Test
    void givenValidOrderRequest_whenCreateOrder_thenOrderCreatedSuccessfully() {
        Mockito.lenient().when(orderRepository.findById(any())).thenReturn(Optional.of(new Order()));
        System.out.println("Hello world");
    }
}
