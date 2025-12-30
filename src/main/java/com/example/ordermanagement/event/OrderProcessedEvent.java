package com.example.ordermanagement.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class OrderProcessedEvent implements DomainEvent {
    
    private final String orderId;
    private final LocalDateTime processedAt;

    @JsonCreator
    public OrderProcessedEvent(@JsonProperty("orderId") String orderId, 
                              @JsonProperty("processedAt") LocalDateTime processedAt) {
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
