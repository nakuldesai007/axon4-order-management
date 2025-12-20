package com.example.ordermanagement.event;

import java.time.LocalDateTime;

public class OrderCreatedEvent implements DomainEvent {
    
    private final String orderId;
    private final String customerId;
    private final String customerName;
    private final String customerEmail;
    private final String shippingAddress;
    private final LocalDateTime createdAt;

    // Default constructor for Jackson deserialization
    public OrderCreatedEvent() {
        this.orderId = null;
        this.customerId = null;
        this.customerName = null;
        this.customerEmail = null;
        this.shippingAddress = null;
        this.createdAt = null;
    }

    public OrderCreatedEvent(String orderId, String customerId, String customerName, 
                           String customerEmail, String shippingAddress, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
        this.createdAt = createdAt;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getShippingAddress() { return shippingAddress; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // DomainEvent interface implementation
    @Override
    public String getAggregateId() { return orderId; }
    
    @Override
    public LocalDateTime getTimestamp() { return createdAt; }
    
    @Override
    public String getEventType() { return "OrderCreatedEvent"; }
}
