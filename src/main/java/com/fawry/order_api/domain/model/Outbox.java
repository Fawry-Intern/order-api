package com.fawry.order_api.domain.model;

import com.fawry.order_api.dto.enums.OrderSagaStatus;
import com.fawry.order_api.dto.enums.SagaEventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "outbox")
@EntityListeners(AuditingEntityListener.class)
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "saga_event_type", nullable = false, length = 50)
    private SagaEventType sagaEventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderSagaStatus status;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_contact", length = 20)
    private String customerContact;

    @Column(name = "governorate", length = 100)
    private String governorate;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "payment_amount", precision = 19, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "number", length = 20)
    private String number;

    @Column(name = "cvv", length = 10)
    private String cvv;

    @Column(name = "expiry", length = 10)
    private String expiry;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private Instant createdAt;

    @NotNull
    @Column(name = "processed", nullable = false)
    private Boolean processed;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;
}


