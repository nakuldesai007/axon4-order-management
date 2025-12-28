package com.example.ordermanagement.aggregate;

import com.example.ordermanagement.command.*;
import com.example.ordermanagement.event.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.modelling.command.AggregateVersion;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Aggregate
public class Order {

    @AggregateIdentifier
    private String orderId;
    
    @AggregateVersion
    private Long version;
    
    private String customerId;
    private String customerName;
    private String customerEmail;
    private LocalDateTime createdAt;
    private String shippingAddress;
    
    @AggregateMember
    private OrderItems orderItems;
    
    @AggregateMember
    private OrderStatusManager statusManager;

    // Required by Axon
    protected Order() {}

    // Command handlers
    @CommandHandler
    public Order(CreateOrderCommand command) {
        AggregateLifecycle.apply(new OrderCreatedEvent(
                command.getOrderId(),
                command.getCustomerId(),
                command.getCustomerName(),
                command.getCustomerEmail(),
                command.getShippingAddress(),
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(AddItemToOrderCommand command) {
        if (!statusManager.isCreated()) {
            throw new IllegalStateException("Cannot add items to order in status: " + statusManager.getStatus());
        }

        if (command.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (command.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        publishEvent(new ItemAddedToOrderEvent(
                orderId,
                command.getProductId(),
                command.getProductName(),
                command.getQuantity(),
                command.getPrice(),
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(RemoveItemFromOrderCommand command) {
        if (!statusManager.isCreated()) {
            throw new IllegalStateException("Cannot remove items from order in status: " + statusManager.getStatus());
        }

        boolean itemExists = orderItems.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(command.getProductId()));

        if (!itemExists) {
            throw new IllegalArgumentException("Item not found in order");
        }

        publishEvent(new ItemRemovedFromOrderEvent(
                orderId,
                command.getProductId(),
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(ConfirmOrderCommand command) {
        if (!statusManager.isCreated()) {
            throw new IllegalStateException("Order must be in CREATED status to confirm");
        }

        if (orderItems.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot confirm order without items");
        }

        publishEvent(new OrderConfirmedEvent(
                orderId,
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(ProcessOrderCommand command) {
        if (!statusManager.isConfirmed()) {
            throw new IllegalStateException("Order must be in CONFIRMED status to process");
        }

        publishEvent(new OrderProcessedEvent(
                orderId,
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(ShipOrderCommand command) {
        if (!statusManager.isProcessed()) {
            throw new IllegalStateException("Order must be in PROCESSED status to ship");
        }

        if (command.getTrackingNumber() == null || command.getTrackingNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Tracking number is required");
        }

        publishEvent(new OrderShippedEvent(
                orderId,
                command.getTrackingNumber(),
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(CancelOrderCommand command) {
        if (statusManager.isShipped()) {
            throw new IllegalStateException("Cannot cancel shipped order");
        }

        if (command.getReason() == null || command.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        publishEvent(new OrderCancelledEvent(
                orderId,
                command.getReason(),
                LocalDateTime.now()
        ));
    }

    @CommandHandler
    public void handle(UpdateShippingAddressCommand command) {
        if (statusManager.isShipped() || statusManager.isCancelled()) {
            throw new IllegalStateException("Cannot update shipping address for an order that is shipped or cancelled.");
        }

        if (command.getShippingAddress() == null || command.getShippingAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Shipping address cannot be empty.");
        }

        if (command.getShippingAddress().equals(this.shippingAddress)) {
            return;
        }

        publishEvent(new ShippingAddressUpdatedEvent(
                orderId,
                command.getShippingAddress(),
                LocalDateTime.now()
        ));
    }

    // Common event publishing method
    private void publishEvent(DomainEvent event) {
        AggregateLifecycle.apply(event);
    }

    // Event sourcing handlers
    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
        this.customerId = event.getCustomerId();
        this.customerName = event.getCustomerName();
        this.customerEmail = event.getCustomerEmail();
        this.shippingAddress = event.getShippingAddress();
        this.createdAt = event.getCreatedAt();
        
        // Initialize aggregate members
        this.orderItems = new OrderItems();
        this.statusManager = new OrderStatusManager();
    }

    @EventSourcingHandler
    public void on(ShippingAddressUpdatedEvent event) {
        this.shippingAddress = event.getShippingAddress();
    }

    // Getters
    public String getOrderId() { return orderId; }
    public Long getVersion() { return version; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getShippingAddress() { return shippingAddress; }
    public OrderItems getOrderItems() { return orderItems; }
    public OrderStatusManager getStatusManager() { return statusManager; }

    // Inner classes
    public static class OrderItem {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal price;

        public OrderItem() {}

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

    public enum OrderStatus {
        CREATED, CONFIRMED, PROCESSED, SHIPPED, DELIVERED, CANCELLED
    }
}