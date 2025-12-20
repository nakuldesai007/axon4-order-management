package com.example.ordermanagement.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class ShipOrderCommand {
    
    @TargetAggregateIdentifier
    private final String orderId;
    private final String trackingNumber;

    public ShipOrderCommand(String orderId, String trackingNumber) {
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getTrackingNumber() { return trackingNumber; }
} 