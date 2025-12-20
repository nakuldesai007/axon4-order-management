package com.example.ordermanagement.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ItemAddedToOrderEvent implements DomainEvent {
    
    private final String orderId;
    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal price;
    private final LocalDateTime addedAt;

    // Default constructor for Jackson deserialization
    public ItemAddedToOrderEvent() {
        this.orderId = null;
        this.productId = null;
        this.productName = null;
        this.quantity = 0;
        this.price = null;
        this.addedAt = null;
    }

    public ItemAddedToOrderEvent(String orderId, String productId, String productName, 
                                int quantity, BigDecimal price, LocalDateTime addedAt) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.addedAt = addedAt;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public LocalDateTime getAddedAt() { return addedAt; }

    // DomainEvent interface implementation
    @Override
    public String getAggregateId() { return orderId; }
    
    @Override
    public LocalDateTime getTimestamp() { return addedAt; }
    
    @Override
    public String getEventType() { return "ItemAddedToOrderEvent"; }
}
