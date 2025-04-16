package com.fawry.order_api.infrastructure.jobqueue;

import com.fawry.order_api.dto.dtos.OrderCreationJob;

import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderJobProducer {

    private final RedisTemplate<String, Object> redis;
    private final OrderMapper orderMapper;

    @Value("${redis.job.queue.name}")
    private String QUEUE_NAME;


    @Async("orderTaskExecutor")
    public CompletableFuture<OrderCreationJob> addJob(OrderRequest job) {
        log.info("Thread name: {}", Thread.currentThread().getName());
        log.info("job ",job.toString());
        redis.opsForList().leftPush(QUEUE_NAME, job);
        return CompletableFuture.completedFuture(orderMapper.mapToOrderCreationJob(job));
    }
}
