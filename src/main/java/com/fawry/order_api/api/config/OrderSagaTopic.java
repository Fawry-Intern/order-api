package com.fawry.order_api.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class OrderSagaTopic {

    @Value("${custom.order.topic.name}")
    private String TOPIC_NAME;
    @Value("${custom.order.topic.total_partitions}")
    private int TOTAL_PARTITIONS;

    @Bean
    public NewTopic OrderSagaTopic() {
        return TopicBuilder
                .name(TOPIC_NAME)
                .partitions(TOTAL_PARTITIONS)
                .replicas(1)
                .build();
    }

}
