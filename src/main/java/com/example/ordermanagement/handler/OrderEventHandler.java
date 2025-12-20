package com.example.ordermanagement.handler;

import com.example.ordermanagement.event.*;
import com.example.ordermanagement.query.OrderItemSummary;
import com.example.ordermanagement.query.OrderSummary;
import com.example.ordermanagement.query.OrderSummaryRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderEventHandler {

    private final OrderSummaryRepository orderSummaryRepository;

    public OrderEventHandler(OrderSummaryRepository orderSummaryRepository) {
        this.orderSummaryRepository = orderSummaryRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) {
        OrderSummary orderSummary = new OrderSummary(
                event.getOrderId(),
                event.getCustomerId(),
                event.getCustomerName(),
                event.getCustomerEmail(),
                event.getShippingAddress()
        );
        orderSummary.setCreatedAt(event.getCreatedAt());
        orderSummary.setUpdatedAt(event.getCreatedAt());
        
        orderSummaryRepository.save(orderSummary);
    }

    @EventHandler
    public void on(ItemAddedToOrderEvent event) {
        OrderSummary orderSummary = orderSummaryRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        // Remove existing item if it exists
        orderSummary.removeItem(event.getProductId());
        
        // Add new item
        OrderItemSummary item = new OrderItemSummary(
                event.getProductId(),
                event.getProductName(),
                event.getQuantity(),
                event.getPrice()
        );
        orderSummary.addItem(item);
        orderSummary.setUpdatedAt(event.getAddedAt());
        
        orderSummaryRepository.save(orderSummary);
    }

    @EventHandler
    public void on(ItemRemovedFromOrderEvent event) {
        OrderSummary orderSummary = orderSummaryRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        orderSummary.removeItem(event.getProductId());
        orderSummary.setUpdatedAt(event.getRemovedAt());
        
        orderSummaryRepository.save(orderSummary);
    }

    @EventHandler
    public void on(OrderConfirmedEvent event) {
        OrderSummary orderSummary = orderSummaryRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        orderSummary.setStatus(OrderSummary.OrderStatus.CONFIRMED);
        orderSummary.setUpdatedAt(event.getConfirmedAt());
        
        orderSummaryRepository.save(orderSummary);
    }

    @EventHandler
    public void on(OrderProcessedEvent event) {
        OrderSummary orderSummary = orderSummaryRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        orderSummary.setStatus(OrderSummary.OrderStatus.PROCESSED);
        orderSummary.setUpdatedAt(event.getProcessedAt());
        
        orderSummaryRepository.save(orderSummary);
    }

    @EventHandler
    public void on(OrderShippedEvent event) {
        OrderSummary orderSummary = orderSummaryRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        orderSummary.setStatus(OrderSummary.OrderStatus.SHIPPED);
        orderSummary.setTrackingNumber(event.getTrackingNumber());
        orderSummary.setUpdatedAt(event.getShippedAt());
        
        orderSummaryRepository.save(orderSummary);
    }

    @EventHandler
    public void on(OrderCancelledEvent event) {
        OrderSummary orderSummary = orderSummaryRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        orderSummary.setStatus(OrderSummary.OrderStatus.CANCELLED);
        orderSummary.setCancellationReason(event.getReason());
        orderSummary.setUpdatedAt(event.getCancelledAt());
        
        orderSummaryRepository.save(orderSummary);
    }
} 