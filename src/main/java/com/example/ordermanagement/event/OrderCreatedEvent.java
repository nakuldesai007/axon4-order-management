package com.example.ordermanagement.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class OrderCreatedEvent implements DomainEvent {
    
    private final String orderId;
    private final String customerId;
    private final String customerName;
    private final String customerEmail;
    private final String shippingAddress;
    private final LocalDateTime createdAt;

    @JsonCreator
    public OrderCreatedEvent(@JsonProperty("orderId") String orderId, 
                           @JsonProperty("customerId") String customerId, 
                           @JsonProperty("customerName") String customerName, 
                           @JsonProperty("customerEmail") String customerEmail, 
                           @JsonProperty("shippingAddress") String shippingAddress, 
                           @JsonProperty("createdAt") LocalDateTime createdAt) {
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
