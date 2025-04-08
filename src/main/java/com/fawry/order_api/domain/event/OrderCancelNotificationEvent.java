package com.fawry.order_api.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
@Getter
public class OrderCancelNotificationEvent {

    private final Long orderId;
    private final String reason;
    private final String customerEmail;
    private final Instant cancellationDate;

    public static OrderCancelNotificationEvent newInstance(Long orderId, String reason, String customerEmail, Instant cancellationDate) {
        return new OrderCancelNotificationEvent(orderId, reason, customerEmail, cancellationDate);
    }
}
