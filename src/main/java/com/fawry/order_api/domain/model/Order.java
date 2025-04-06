package com.fawry.order_api.domain.model;
import com.fawry.order_api.dto.enums.OrderSagaStatus;
import com.fawry.order_api.exception.InvalidDiscountException;
import com.fawry.order_api.exception.InvalidTotalAmountException;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import static com.fawry.order_api.dto.enums.OrderSagaStatus.*;

@NamedEntityGraph(
        name = "Order.withOrderItems",
        attributeNodes = @NamedAttributeNode("orderItems")
)
@Getter
@Entity
@Table(name = "orders")
@Slf4j
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "order_sequence", allocationSize = 1)
    @Column(name = "order_id")
    private Long orderId;

    @Setter
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false))
    })
    private Money paymentAmount;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderSagaStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderItem> orderItems;

    public Order() {
    }

    public static Order newInstance(Long userId,
                             Money paymentAmount,
                             String couponCode,
                             Set<OrderItem> orderItems){
        Order order = new Order();
        order.userId = userId;
        order.paymentAmount = validateTotalAmount(paymentAmount, orderItems);
        order.couponCode = couponCode;
        order.status = CREATED;
        order.orderItems = orderItems;
        orderItems.forEach((oi) -> {
            oi.addOrder(order);
            log.info("OrderItem is : {}", oi);
        });
        return order;
    }

    private static Money validateTotalAmount(Money totalAmount, Set<OrderItem> items) {
        Money calculatedTotal = items.stream()
                .map(item -> item.getPrice().multiple(item.getQuantity()))
                .reduce(Money.of(BigDecimal.ZERO), Money::add);

        if (!calculatedTotal.equals(totalAmount)) {
            throw new InvalidTotalAmountException(totalAmount, calculatedTotal);
        }
        return totalAmount;
}

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    public void applyDiscount(Money discount) {
        if (discount.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDiscountException("Discount cannot be negative");
        }
        this.paymentAmount = this.paymentAmount.subtract(discount);
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.status.equals(CREATED)) {
            this.status = CANCELED;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
