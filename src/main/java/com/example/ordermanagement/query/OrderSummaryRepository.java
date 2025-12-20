package com.example.ordermanagement.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderSummaryRepository extends JpaRepository<OrderSummary, String> {
    
    List<OrderSummary> findByCustomerId(String customerId);
    
    List<OrderSummary> findByStatus(OrderSummary.OrderStatus status);
    
    @Query("SELECT o FROM OrderSummary o WHERE o.customerName LIKE %:customerName%")
    List<OrderSummary> findByCustomerNameContaining(@Param("customerName") String customerName);
    
    @Query("SELECT o FROM OrderSummary o WHERE o.totalAmount >= :minAmount")
    List<OrderSummary> findByTotalAmountGreaterThanEqual(@Param("minAmount") java.math.BigDecimal minAmount);
    
    @Query("SELECT COUNT(o) FROM OrderSummary o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderSummary.OrderStatus status);
    
    @Query("SELECT AVG(o.totalAmount) FROM OrderSummary o WHERE o.status = :status")
    Optional<java.math.BigDecimal> getAverageOrderValueByStatus(@Param("status") OrderSummary.OrderStatus status);
} 