package com.example.ordermanagement.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ItemRemovedFromOrderEvent implements DomainEvent {
    
    private final String orderId;
    private final String productId;
    private final LocalDateTime removedAt;

    @JsonCreator
    public ItemRemovedFromOrderEvent(@JsonProperty("orderId") String orderId, 
                                    @JsonProperty("productId") String productId, 
                                    @JsonProperty("removedAt") LocalDateTime removedAt) {
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
