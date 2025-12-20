package com.example.ordermanagement.event;

import java.time.LocalDateTime;

public class OrderCancelledEvent implements DomainEvent {
    
    private final String orderId;
    private final String reason;
    private final LocalDateTime cancelledAt;

    // Default constructor for Jackson deserialization
    public OrderCancelledEvent() {
        this.orderId = null;
        this.reason = null;
        this.cancelledAt = null;
    }

    public OrderCancelledEvent(String orderId, String reason, LocalDateTime cancelledAt) {
        this.orderId = orderId;
        this.reason = reason;
        this.cancelledAt = cancelledAt;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getReason() { return reason; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }

    // DomainEvent interface implementation
    @Override
    public String getAggregateId() { return orderId; }
    
    @Override
    public LocalDateTime getTimestamp() { return cancelledAt; }
    
    @Override
    public String getEventType() { return "OrderCancelledEvent"; }
}
