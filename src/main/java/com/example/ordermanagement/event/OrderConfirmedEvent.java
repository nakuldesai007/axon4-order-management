package com.example.ordermanagement.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class OrderConfirmedEvent implements DomainEvent {
    
    private final String orderId;
    private final LocalDateTime confirmedAt;

    @JsonCreator
    public OrderConfirmedEvent(@JsonProperty("orderId") String orderId, 
                              @JsonProperty("confirmedAt") LocalDateTime confirmedAt) {
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
