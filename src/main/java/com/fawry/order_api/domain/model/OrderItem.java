package com.fawry.order_api.domain.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fawry.order_api.exception.InvalidOrderException;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price", nullable = false))
    })
    private Money price;

    private OrderItem() {
    }

    public static OrderItem newInstance(Long storeId, Long productId, Integer quantity, Money price) {
        var orderItem = new OrderItem();
        orderItem.storeId = storeId;
        orderItem.productId = productId;
        orderItem.quantity = quantity;
        orderItem.price = price;

        return orderItem;
    }

    public void setOrder(Order order) {
        if (order == null) {
            throw new InvalidOrderException("Order cannot be null");
        }
        this.order = order;
    }

    @Override
    public String toString() {
        return "OrderItem{" + "itemId=" + itemId + ", productId=" + productId + ", order=" + order +
                ", quantity=" + quantity + ", price=" + price + '}';
    }
}
