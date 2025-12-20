package com.example.ordermanagement.aggregate;

import com.example.ordermanagement.event.ItemAddedToOrderEvent;
import com.example.ordermanagement.event.ItemRemovedFromOrderEvent;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateMember;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderItems {
    
    @AggregateMember
    private List<OrderItem> items = new ArrayList<>();
    
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    // Event sourcing handlers for item-related events
    @EventSourcingHandler
    public void on(ItemAddedToOrderEvent event) {
        // Remove existing item if it exists
        items.removeIf(item -> item.getProductId().equals(event.getProductId()));
        
        // Add new item
        OrderItem newItem = new OrderItem(
                event.getProductId(),
                event.getProductName(),
                event.getQuantity(),
                event.getPrice()
        );
        items.add(newItem);
        
        // Recalculate total
        recalculateTotal();
    }
    
    @EventSourcingHandler
    public void on(ItemRemovedFromOrderEvent event) {
        items.removeIf(item -> item.getProductId().equals(event.getProductId()));
        recalculateTotal();
    }
    
    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Getters
    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public boolean containsItem(String productId) {
        return items.stream().anyMatch(item -> item.getProductId().equals(productId));
    }
    
    // Inner class for OrderItem
    public static class OrderItem {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal price;

        public OrderItem(String productId, String productName, int quantity, BigDecimal price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }

        // Getters
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
    }
} 