package com.fawry.order_api.infrastructure.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TransactionExecutor  {

    private final PlatformTransactionManager transactionManager;

    public  <T> T executeInTransaction(Supplier<T> operation) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            T result = operation.get();
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new RuntimeException("Transaction failed", e);
        }
    }
}
