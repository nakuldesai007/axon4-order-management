package com.example.ordermanagement.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class RemoveItemFromOrderCommand {
    
    @TargetAggregateIdentifier
    private final String orderId;
    private final String productId;

    public RemoveItemFromOrderCommand(String orderId, String productId) {
        this.orderId = orderId;
        this.productId = productId;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
} 