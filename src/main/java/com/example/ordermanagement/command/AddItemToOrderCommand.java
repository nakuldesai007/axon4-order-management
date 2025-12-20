package com.example.ordermanagement.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;

public class AddItemToOrderCommand {
    
    @TargetAggregateIdentifier
    private final String orderId;
    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal price;

    public AddItemToOrderCommand(String orderId, String productId, String productName, 
                                int quantity, BigDecimal price) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
} 