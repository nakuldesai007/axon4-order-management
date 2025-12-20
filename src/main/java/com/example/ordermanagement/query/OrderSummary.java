package com.example.ordermanagement.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_summaries")
@Schema(description = "Order summary information for query model")
public class OrderSummary {
    
    @Id
    @Schema(description = "Unique order identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderId;
    
    @Schema(description = "Customer ID", example = "CUST-001")
    private String customerId;
    
    @Schema(description = "Customer name", example = "John Doe")
    private String customerName;
    
    @Schema(description = "Customer email", example = "john.doe@example.com")
    private String customerEmail;
    
    @Schema(description = "Shipping address", example = "123 Main St, City, State 12345")
    private String shippingAddress;
    
    @Schema(description = "Total order amount", example = "999.99")
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Schema(description = "Order status", example = "CREATED")
    private OrderStatus status;
    
    @Schema(description = "Order creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Shipping tracking number", example = "TRK123456789")
    private String trackingNumber;
    
    @Schema(description = "Cancellation reason", example = "Customer requested cancellation")
    private String cancellationReason;
    
    @OneToMany(mappedBy = "orderSummary", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItemSummary> items = new ArrayList<>();

    // Required by JPA
    protected OrderSummary() {}

    public OrderSummary(String orderId, String customerId, String customerName, 
                       String customerEmail, String shippingAddress) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
        this.totalAmount = BigDecimal.ZERO;
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    public List<OrderItemSummary> getItems() { return items; }
    public void setItems(List<OrderItemSummary> items) { this.items = items; }

    public void addItem(OrderItemSummary item) {
        items.add(item);
        item.setOrderSummary(this);
        recalculateTotal();
    }

    public void removeItem(String productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
        recalculateTotal();
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public enum OrderStatus {
        CREATED, CONFIRMED, PROCESSED, SHIPPED, DELIVERED, CANCELLED
    }
} 