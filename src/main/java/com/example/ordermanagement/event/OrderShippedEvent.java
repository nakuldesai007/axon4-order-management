package com.example.ordermanagement.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class OrderShippedEvent implements DomainEvent {
    
    private final String orderId;
    private final String trackingNumber;
    private final LocalDateTime shippedAt;

    @JsonCreator
    public OrderShippedEvent(@JsonProperty("orderId") String orderId, 
                            @JsonProperty("trackingNumber") String trackingNumber, 
                            @JsonProperty("shippedAt") LocalDateTime shippedAt) {
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
