package com.example.ordermanagement.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CreateOrderCommand {
    
    @TargetAggregateIdentifier
    private final String orderId;
    private final String customerId;
    private final String customerName;
    private final String customerEmail;
    private final String shippingAddress;

    public CreateOrderCommand(String orderId, String customerId, String customerName, 
                            String customerEmail, String shippingAddress) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getShippingAddress() { return shippingAddress; }
} 