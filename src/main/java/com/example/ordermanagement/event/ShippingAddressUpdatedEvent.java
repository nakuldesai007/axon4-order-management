package com.example.ordermanagement.event;

import java.time.LocalDateTime;

public class ShippingAddressUpdatedEvent implements DomainEvent {

    private final String orderId;
    private final String shippingAddress;
    private final LocalDateTime updatedAt;

    public ShippingAddressUpdatedEvent(String orderId, String shippingAddress, LocalDateTime updatedAt) {
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
