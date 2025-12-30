package com.example.ordermanagement.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ItemAddedToOrderEvent implements DomainEvent {
    
    private final String orderId;
    private final String productId;
    private final String productName;
    private final int quantity;
    private final BigDecimal price;
    private final LocalDateTime addedAt;

    @JsonCreator
    public ItemAddedToOrderEvent(@JsonProperty("orderId") String orderId, 
                                @JsonProperty("productId") String productId, 
                                @JsonProperty("productName") String productName, 
                                @JsonProperty("quantity") int quantity, 
                                @JsonProperty("price") BigDecimal price, 
                                @JsonProperty("addedAt") LocalDateTime addedAt) {
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
