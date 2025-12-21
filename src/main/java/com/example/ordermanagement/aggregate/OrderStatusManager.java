package com.example.ordermanagement.aggregate;

import com.example.ordermanagement.event.OrderCancelledEvent;
import com.example.ordermanagement.event.OrderConfirmedEvent;
import com.example.ordermanagement.event.OrderCreatedEvent;
import com.example.ordermanagement.event.OrderProcessedEvent;
import com.example.ordermanagement.event.OrderShippedEvent;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateMember;

import java.time.LocalDateTime;

public class OrderStatusManager {
    
    @AggregateMember
    private OrderStatus status;
    
    private LocalDateTime updatedAt;
    
    // Event sourcing handlers for status-related events
    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        this.status = OrderStatus.CREATED;
        this.updatedAt = event.getCreatedAt();
    }
    
    @EventSourcingHandler
    public void on(OrderConfirmedEvent event) {
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = event.getConfirmedAt();
    }
    
    @EventSourcingHandler
    public void on(OrderProcessedEvent event) {
        this.status = OrderStatus.PROCESSED;
        this.updatedAt = event.getProcessedAt();
    }
    
    @EventSourcingHandler
    public void on(OrderShippedEvent event) {
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = event.getShippedAt();
    }
    
    @EventSourcingHandler
    public void on(OrderCancelledEvent event) {
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = event.getCancelledAt();
    }
    
    // Getters
    public OrderStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public boolean isCreated() {
        return status == OrderStatus.CREATED;
    }
    
    public boolean isConfirmed() {
        return status == OrderStatus.CONFIRMED;
    }
    
    public boolean isProcessed() {
        return status == OrderStatus.PROCESSED;
    }
    
    public boolean isShipped() {
        return status == OrderStatus.SHIPPED;
    }
    
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }
    
    public boolean canBeCancelled() {
        return !isShipped() && status != OrderStatus.DELIVERED;
    }
    
    public enum OrderStatus {
        CREATED, CONFIRMED, PROCESSED, SHIPPED, DELIVERED, CANCELLED
    }
} 