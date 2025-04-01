package com.fawry.order_api.entities;
import com.fawry.order_api.dto.enums.OrderSagaStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import static com.fawry.order_api.dto.enums.OrderSagaStatus.RECEIVED;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "orders")
@Slf4j
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "order_sequence", allocationSize = 1)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderSagaStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<OrderItem> orderItems;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public static Order newInstance(Long userId,
                                    BigDecimal totalAmount,
                                    String couponCode,
                                    Set<OrderItem> orderItems){
        Order order = new Order();
        order.userId = userId;
        order.totalAmount = validateTotalAmount(totalAmount, orderItems);
        order.couponCode = couponCode;
        order.status = RECEIVED;
        order.orderItems = orderItems;
        orderItems.forEach((oi) -> {
            oi.addOrder(order);
            log.info("OrderItem is : {}", oi);
        });
        return order;
    }

    public static BigDecimal validateTotalAmount(BigDecimal totalAmount, Set<OrderItem> items) {
        BigDecimal calculatedTotal = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (calculatedTotal.equals(totalAmount)) {
            return totalAmount;
        }

        throw new IllegalArgumentException("Total amount does not match calculated total");
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    public void applyDiscount(BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount cannot be native");
        }
        this.totalAmount = this.totalAmount.subtract(discount);
    }
}
