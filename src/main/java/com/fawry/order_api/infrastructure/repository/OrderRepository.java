package com.fawry.order_api.infrastructure.repository;

import com.fawry.order_api.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(value = "Order.withOrderItems", type = EntityGraph.EntityGraphType.FETCH)
    @Query(
            value = "SELECT o FROM Order o WHERE o.userId = :userId AND o.createdAt BETWEEN :startDate AND :endDate",
            countQuery = "SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.createdAt BETWEEN :startDate AND :endDate"
    )
    Page<Order> findByUserIdAndDateRange(@Param("userId") Long userId,
                                         @Param("startDate") Instant startDate,
                                         @Param("endDate") Instant endDate,
                                         Pageable pageable);
}
