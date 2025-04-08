package com.fawry.order_api.application.service;

public interface OutboxService {
    void eventProcessing();
}
