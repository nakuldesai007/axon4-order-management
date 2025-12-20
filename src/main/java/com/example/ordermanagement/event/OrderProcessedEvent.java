package com.example.ordermanagement.event;

import java.time.LocalDateTime;

public class OrderProcessedEvent implements DomainEvent {
    
    private final String orderId;
    private final LocalDateTime processedAt;

    // Default constructor for Jackson deserialization
    public OrderProcessedEvent() {
        this.orderId = null;
        this.processedAt = null;
    }

    public OrderProcessedEvent(String orderId, LocalDateTime processedAt) {
        this.orderId = orderId;
        this.processedAt = processedAt;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public LocalDateTime getProcessedAt() { return processedAt; }

    // DomainEvent interface implementation
    @Override
    public String getAggregateId() { return orderId; }
    
    @Override
    public LocalDateTime getTimestamp() { return processedAt; }
    
    @Override
    public String getEventType() { return "OrderProcessedEvent"; }
}
