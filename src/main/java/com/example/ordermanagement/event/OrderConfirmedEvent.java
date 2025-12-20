package com.example.ordermanagement.event;

import java.time.LocalDateTime;

public class OrderConfirmedEvent implements DomainEvent {
    
    private final String orderId;
    private final LocalDateTime confirmedAt;

    // Default constructor for Jackson deserialization
    public OrderConfirmedEvent() {
        this.orderId = null;
        this.confirmedAt = null;
    }

    public OrderConfirmedEvent(String orderId, LocalDateTime confirmedAt) {
        this.orderId = orderId;
        this.confirmedAt = confirmedAt;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }

    // DomainEvent interface implementation
    @Override
    public String getAggregateId() { return orderId; }
    
    @Override
    public LocalDateTime getTimestamp() { return confirmedAt; }
    
    @Override
    public String getEventType() { return "OrderConfirmedEvent"; }
}
