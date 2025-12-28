package com.example.ordermanagement.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class UpdateShippingAddressCommand {

    @TargetAggregateIdentifier
    private final String orderId;
    private final String shippingAddress;

    public UpdateShippingAddressCommand(String orderId, String shippingAddress) {
        this.orderId = orderId;
        this.shippingAddress = shippingAddress;
    }

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }
}
