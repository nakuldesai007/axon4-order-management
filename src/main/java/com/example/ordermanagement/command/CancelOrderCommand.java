package com.example.ordermanagement.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CancelOrderCommand {
    
    @TargetAggregateIdentifier
    private final String orderId;
    private final String reason;

    public CancelOrderCommand(String orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getReason() { return reason; }
} 