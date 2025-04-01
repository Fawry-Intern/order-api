package com.fawry.order_api.entities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    public static OrderItem newInstance(Long productId, Integer quantity, BigDecimal price) {
        var orderItem = new OrderItem();
        orderItem.productId = productId;
        orderItem.quantity = quantity;
        orderItem.price = price;

        return orderItem;
    }

    public void addOrder(Order order) {
        if (order == null) {
            throw new RuntimeException("Order is null");
        }
        this.order = order;
    }

    @Override
    public String toString() {
        return "OrderItem{" + "itemId=" + itemId + ", productId=" + productId + ", order=" + order +
                ", quantity=" + quantity + ", price=" + price + '}';
    }
}
