package com.fawry.kafka.events;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class SagaCanceledEventDto {
    private Long orderId;
    private String reason;
    private String customerEmail;
}
