package com.example.ordermanagement.query;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_item_summaries")
public class OrderItemSummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderSummary orderSummary;

    // Required by JPA
    protected OrderItemSummary() {}

    public OrderItemSummary(String productId, String productName, int quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public OrderSummary getOrderSummary() { return orderSummary; }
    public void setOrderSummary(OrderSummary orderSummary) { this.orderSummary = orderSummary; }
} 