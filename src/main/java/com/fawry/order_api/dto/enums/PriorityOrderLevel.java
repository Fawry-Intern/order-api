package com.fawry.order_api.dto.enums;

public enum PriorityOrderLevel {

    HIGH("order-create-queue"),
    LOW("order-cancel-queue");

    private final String queueName;

    PriorityOrderLevel(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }

    public static PriorityOrderLevel fromString(String priority) {
        for (PriorityOrderLevel level : PriorityOrderLevel.values()) {
            if (level.queueName.equalsIgnoreCase(priority)) {
                return level;
            }
        }
        return LOW;
    }
}
