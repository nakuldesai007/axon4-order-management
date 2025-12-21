package com.example.ordermanagement;

import com.example.ordermanagement.controller.OrderController;
import com.example.ordermanagement.query.OrderSummary;
import com.example.ordermanagement.query.OrderSummaryRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderControllerCreateOrderTest {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;

    @Autowired
    private CommandGateway commandGateway;

    @BeforeEach
    void setUp() {
        orderSummaryRepository.deleteAll();
    }

    @Test
    void testCreateOrder_WithValidRequest_ShouldReturnOrderId() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-001");
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john.doe@example.com");
        request.setShippingAddress("123 Main St, City, State 12345");

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        assertNotNull(orderId);
        assertFalse(orderId.isEmpty());
        assertTrue(UUID.fromString(orderId).toString().equals(orderId), "Order ID should be a valid UUID");
    }

    @Test
    void testCreateOrder_WithValidRequest_ShouldCreateOrderInRepository() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-002");
        request.setCustomerName("Jane Smith");
        request.setCustomerEmail("jane.smith@example.com");
        request.setShippingAddress("456 Oak Ave, City, State 12345");

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        // Wait a bit for event handler to process
        Thread.sleep(100);

        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order, "Order should be created in repository");
        assertEquals("CUST-002", order.getCustomerId());
        assertEquals("Jane Smith", order.getCustomerName());
        assertEquals("jane.smith@example.com", order.getCustomerEmail());
        assertEquals("456 Oak Ave, City, State 12345", order.getShippingAddress());
        assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void testCreateOrder_WithNullCustomerId_ShouldStillCreateOrder() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId(null);
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john.doe@example.com");
        request.setShippingAddress("123 Main St");

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        assertNotNull(orderId);
        
        Thread.sleep(100);
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertNull(order.getCustomerId());
    }

    @Test
    void testCreateOrder_WithNullCustomerName_ShouldStillCreateOrder() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-003");
        request.setCustomerName(null);
        request.setCustomerEmail("test@example.com");
        request.setShippingAddress("123 Main St");

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        assertNotNull(orderId);
        
        Thread.sleep(100);
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertNull(order.getCustomerName());
    }

    @Test
    void testCreateOrder_WithNullEmail_ShouldStillCreateOrder() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-004");
        request.setCustomerName("John Doe");
        request.setCustomerEmail(null);
        request.setShippingAddress("123 Main St");

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        assertNotNull(orderId);
        
        Thread.sleep(100);
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertNull(order.getCustomerEmail());
    }

    @Test
    void testCreateOrder_WithNullShippingAddress_ShouldStillCreateOrder() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-005");
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john.doe@example.com");
        request.setShippingAddress(null);

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        assertNotNull(orderId);
        
        Thread.sleep(100);
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertNull(order.getShippingAddress());
    }

    @Test
    void testCreateOrder_WithEmptyStrings_ShouldStillCreateOrder() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("");
        request.setCustomerName("");
        request.setCustomerEmail("");
        request.setShippingAddress("");

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        assertNotNull(orderId);
        
        Thread.sleep(100);
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
    }

    @Test
    void testCreateOrder_MultipleOrders_ShouldCreateDifferentOrderIds() throws Exception {
        OrderController.CreateOrderRequest request1 = new OrderController.CreateOrderRequest();
        request1.setCustomerId("CUST-006");
        request1.setCustomerName("Customer One");
        request1.setCustomerEmail("customer1@example.com");
        request1.setShippingAddress("Address 1");

        OrderController.CreateOrderRequest request2 = new OrderController.CreateOrderRequest();
        request2.setCustomerId("CUST-006");
        request2.setCustomerName("Customer One");
        request2.setCustomerEmail("customer1@example.com");
        request2.setShippingAddress("Address 1");

        CompletableFuture<String> future1 = orderController.createOrder(request1);
        CompletableFuture<String> future2 = orderController.createOrder(request2);

        String orderId1 = future1.get(5, TimeUnit.SECONDS);
        String orderId2 = future2.get(5, TimeUnit.SECONDS);

        assertNotNull(orderId1);
        assertNotNull(orderId2);
        assertNotEquals(orderId1, orderId2, "Each order should have a unique ID");
    }

    @Test
    void testCreateOrder_WithSpecialCharacters_ShouldHandleCorrectly() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-007");
        request.setCustomerName("José O'Brien-Smith");
        request.setCustomerEmail("jose.obrien+test@example.co.uk");
        request.setShippingAddress("123 Main St, Apt #4B, New York, NY 10001");

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        assertNotNull(orderId);
        
        Thread.sleep(100);
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals("José O'Brien-Smith", order.getCustomerName());
        assertEquals("jose.obrien+test@example.co.uk", order.getCustomerEmail());
        assertEquals("123 Main St, Apt #4B, New York, NY 10001", order.getShippingAddress());
    }

    @Test
    void testCreateOrder_VerifyOrderStatusIsCreated() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-008");
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setShippingAddress("123 Test St");

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        Thread.sleep(100);
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
    }

    @Test
    void testCreateOrder_VerifyOrderHasNoItemsInitially() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-009");
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setShippingAddress("123 Test St");

        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);

        Thread.sleep(100);
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertTrue(order.getItems().isEmpty(), "New order should have no items");
        assertNotNull(order.getItems(), "Items list should not be null");
    }

    @Test
    void testCreateOrder_VerifyTimestampsAreSet() throws Exception {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-010");
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setShippingAddress("123 Test St");

        long beforeCreation = System.currentTimeMillis();
        CompletableFuture<String> future = orderController.createOrder(request);
        String orderId = future.get(5, TimeUnit.SECONDS);
        long afterCreation = System.currentTimeMillis();

        Thread.sleep(100);
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        
        // Verify timestamps are within reasonable range
        long createdAtMillis = order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertTrue(createdAtMillis >= beforeCreation && createdAtMillis <= afterCreation + 1000,
                "Created timestamp should be set correctly");
    }
}

