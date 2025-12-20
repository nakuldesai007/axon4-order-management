package com.example.ordermanagement.event;

import java.time.LocalDateTime;

public class OrderShippedEvent implements DomainEvent {
    
    private final String orderId;
    private final String trackingNumber;
    private final LocalDateTime shippedAt;

    // Default constructor for Jackson deserialization
    public OrderShippedEvent() {
        this.orderId = null;
        this.trackingNumber = null;
        this.shippedAt = null;
    }

    public OrderShippedEvent(String orderId, String trackingNumber, LocalDateTime shippedAt) {
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.shippedAt = shippedAt;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getTrackingNumber() { return trackingNumber; }
    public LocalDateTime getShippedAt() { return shippedAt; }

    // DomainEvent interface implementation
    @Override
    public String getAggregateId() { return orderId; }
    
    @Override
    public LocalDateTime getTimestamp() { return shippedAt; }
    
    @Override
    public String getEventType() { return "OrderShippedEvent"; }
}
