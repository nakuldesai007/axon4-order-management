package com.example.ordermanagement.event;

import java.time.LocalDateTime;

public class ItemRemovedFromOrderEvent implements DomainEvent {
    
    private final String orderId;
    private final String productId;
    private final LocalDateTime removedAt;

    // Default constructor for Jackson deserialization
    public ItemRemovedFromOrderEvent() {
        this.orderId = null;
        this.productId = null;
        this.removedAt = null;
    }

    public ItemRemovedFromOrderEvent(String orderId, String productId, LocalDateTime removedAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.removedAt = removedAt;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public LocalDateTime getRemovedAt() { return removedAt; }

    // DomainEvent interface implementation
    @Override
    public String getAggregateId() { return orderId; }
    
    @Override
    public LocalDateTime getTimestamp() { return removedAt; }
    
    @Override
    public String getEventType() { return "ItemRemovedFromOrderEvent"; }
}
