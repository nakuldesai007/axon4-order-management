package com.example.ordermanagement.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class ShippingAddressUpdatedEvent implements DomainEvent {

    private final String orderId;
    private final String shippingAddress;
    private final LocalDateTime updatedAt;

    @JsonCreator
    public ShippingAddressUpdatedEvent(@JsonProperty("orderId") String orderId, 
                                       @JsonProperty("shippingAddress") String shippingAddress, 
                                       @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        this.orderId = orderId;
        this.shippingAddress = shippingAddress;
        this.updatedAt = updatedAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    @Override
    public String getAggregateId() {
        return orderId;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return updatedAt;
    }

    @Override
    public String getEventType() {
        return "ShippingAddressUpdatedEvent";
    }
}
