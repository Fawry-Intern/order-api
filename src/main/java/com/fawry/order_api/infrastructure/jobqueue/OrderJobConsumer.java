package com.fawry.order_api.infrastructure.jobqueue;

import com.fawry.order_api.domain.service.saga.OrderCreationSagaService;
import com.fawry.order_api.dto.dtos.OrderRequest;
import com.fawry.order_api.dto.enums.PriorityOrderLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderJobConsumer {

    private final RedisTemplate<String, Object> redis;
    private final OrderCreationSagaService orderCreationSagaService;
    private final static int MAX_RETRIES = 3;


    @Scheduled(fixedRate = 1000)
    public void dequeue() throws ExecutionException, InterruptedException {
        final String orderCreationQueue = PriorityOrderLevel.HIGH.getQueueName();
        OrderRequest job = (OrderRequest) redis.opsForList().rightPop(orderCreationQueue);

        if (job != null) {
            try {
                processJob(job);
            }catch (Exception e) {
                handleFailure(job, orderCreationQueue);
            }
        }
    }

    private void processJob(OrderRequest job) throws ExecutionException, InterruptedException {
        String retryKey = getRetryKey(job);
        boolean hasRetryCount = Boolean.TRUE.equals(redis.hasKey(retryKey));
        Integer retries = 0;
        if (hasRetryCount) {
             retries = (Integer) redis.opsForValue().get(retryKey);
        }
         orderCreationSagaService.createOrderSaga(job);
    }

    private void handleFailure(OrderRequest job, String queueName) {
        int retries = getRetryCount(job);
        if (retries < MAX_RETRIES) {
            redis.opsForList().leftPush(queueName, job);
            incrementRetry(job);
        }else {
            log.info("Max Retries reached for job: {}", job);
        }
    }

    private int getRetryCount(OrderRequest job) {
        String retryKey = getRetryKey(job);
        redis.opsForValue().setIfAbsent(retryKey, 1);
        Integer retries = (Integer) redis.opsForValue().get(retryKey);
        assert retries != null;
        log.info("retries: {}", retries);
        return retries;
    }

    private void incrementRetry(OrderRequest job) {
        String retryKey = getRetryKey(job);
        int retries = getRetryCount(job);
        retries++;
        redis.opsForValue().set(retryKey, retries);
        log.info("Increment retry for process job {} to retries count {}", job, retries);
    }

    private String getRetryKey(OrderRequest job) {
        int hashCode = job.hashCode();
        return String.valueOf(hashCode);
    }
}
