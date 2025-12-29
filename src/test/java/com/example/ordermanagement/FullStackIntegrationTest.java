package com.example.ordermanagement;

import com.example.ordermanagement.controller.OrderController;
import com.example.ordermanagement.query.OrderSummary;
import com.example.ordermanagement.query.OrderSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full-Stack Integration Tests
 * 
 * These tests simulate real user interactions through the API (same as frontend).
 * They verify:
 * 1. API endpoints work correctly
 * 2. Commands are processed
 * 3. Events are published and handled
 * 4. Read model is updated correctly
 * 5. Complete user workflows
 * 
 * This approach tests the same API the React frontend uses, providing
 * confidence that UI and backend work together correctly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Full-Stack Integration Tests (UI + Backend)")
class FullStackIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/orders";
        orderSummaryRepository.deleteAll(); // Clean state for each test
    }

    // ============================================================================
    // SCENARIO 1: Complete Order Lifecycle (Happy Path)
    // ============================================================================

    @Test
    @DisplayName("Complete order lifecycle: Create → Add Items → Confirm → Process → Ship")
    void testCompleteOrderLifecycle_SimulatesUserFlow() {
        // Step 1: User creates order (UI: User fills form and clicks "Create Order")
        OrderController.CreateOrderRequest createRequest = new OrderController.CreateOrderRequest();
        createRequest.setCustomerId("CUST-FS-001");
        createRequest.setCustomerName("John Doe");
        createRequest.setCustomerEmail("john.doe@example.com");
        createRequest.setShippingAddress("123 Main St, City, State 12345");

        ResponseEntity<String> createResponse = restTemplate.postForEntity(
                baseUrl, createRequest, String.class);

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        String orderId = createResponse.getBody();
        assertNotNull(orderId, "Order ID should be returned");

        // Wait for event processing (UI: Order appears in list)
        waitForEventProcessing();

        OrderSummary order = orderSummaryRepository.findById(orderId).orElseThrow();
        assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
        assertEquals("John Doe", order.getCustomerName());

        // Step 2: User adds items (UI: User adds products to cart)
        addItemToOrder(orderId, "PROD-001", "iPhone 15 Pro", 1, new BigDecimal("999.99"));
        addItemToOrder(orderId, "PROD-002", "AirPods Pro", 1, new BigDecimal("249.99"));

        // Wait for event processing (UI: Cart updates)
        waitForEventProcessing();

        order = orderSummaryRepository.findById(orderId).orElseThrow();
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("1249.98"), order.getTotalAmount());

        // Step 3: User confirms order (UI: User clicks "Confirm Order" button)
        ResponseEntity<Void> confirmResponse = restTemplate.postForEntity(
                baseUrl + "/" + orderId + "/confirm", null, Void.class);

        assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());

        // Wait for event processing (UI: Status changes to "Confirmed")
        waitForEventProcessing();

        // Step 4: Admin processes order (UI: Admin clicks "Process Order")
        ResponseEntity<Void> processResponse = restTemplate.postForEntity(
                baseUrl + "/" + orderId + "/process", null, Void.class);

        assertEquals(HttpStatus.OK, processResponse.getStatusCode());

        waitForEventProcessing();

        // Step 5: Admin ships order (UI: Admin enters tracking number and clicks "Ship")
        OrderController.ShipOrderRequest shipRequest = new OrderController.ShipOrderRequest();
        shipRequest.setTrackingNumber("TRK123456789");

        ResponseEntity<Void> shipResponse = restTemplate.postForEntity(
                baseUrl + "/" + orderId + "/ship", shipRequest, Void.class);

        assertEquals(HttpStatus.OK, shipResponse.getStatusCode());

        waitForEventProcessing();

        // Final verification (UI: Order shows as "Shipped" with tracking number)
        OrderSummary finalOrder = orderSummaryRepository.findById(orderId).orElseThrow();
        assertEquals(OrderSummary.OrderStatus.SHIPPED, finalOrder.getStatus());
        assertEquals("TRK123456789", finalOrder.getTrackingNumber());
        assertEquals(2, finalOrder.getItems().size());
    }

    // ============================================================================
    // SCENARIO 2: Order Cancellation Flow
    // ============================================================================

    @Test
    @DisplayName("User cancels order after confirmation")
    void testOrderCancellation_AfterConfirmation() {
        // Create and confirm order
        String orderId = createOrder("CUST-FS-002", "Jane Smith", "jane@example.com", "456 Oak St");
        addItemToOrder(orderId, "PROD-003", "Laptop", 1, new BigDecimal("1299.99"));
        confirmOrder(orderId);

        waitForEventProcessing();

        // User cancels order (UI: User clicks "Cancel Order" and enters reason)
        OrderController.CancelOrderRequest cancelRequest = new OrderController.CancelOrderRequest();
        cancelRequest.setReason("Changed my mind");

        ResponseEntity<Void> cancelResponse = restTemplate.postForEntity(
                baseUrl + "/" + orderId + "/cancel", cancelRequest, Void.class);

        assertEquals(HttpStatus.OK, cancelResponse.getStatusCode());

        // Wait for event processing (UI: Order shows as "Cancelled")
        waitForEventProcessing();

        OrderSummary cancelledOrder = orderSummaryRepository.findById(orderId).orElseThrow();
        assertEquals(OrderSummary.OrderStatus.CANCELLED, cancelledOrder.getStatus());
        assertEquals("Changed my mind", cancelledOrder.getCancellationReason());
    }

    // ============================================================================
    // SCENARIO 3: Multiple Orders Management
    // ============================================================================

    @Test
    @DisplayName("User creates and manages multiple orders")
    void testMultipleOrders_UserManagesMultipleOrders() {
        // User creates first order
        String orderId1 = createOrder("CUST-FS-003", "Bob Wilson", "bob@example.com", "789 Pine Rd");
        addItemToOrder(orderId1, "PROD-004", "Tablet", 1, new BigDecimal("499.99"));

        // User creates second order
        String orderId2 = createOrder("CUST-FS-003", "Bob Wilson", "bob@example.com", "789 Pine Rd");
        addItemToOrder(orderId2, "PROD-005", "Mouse", 2, new BigDecimal("29.99"));

        // Wait for both orders to be processed
        waitForEventProcessing();

        // User confirms first order
        confirmOrder(orderId1);
        waitForEventProcessing();

        // First order is confirmed, second is still CREATED
        OrderSummary order1 = orderSummaryRepository.findById(orderId1).orElseThrow();
        OrderSummary order2 = orderSummaryRepository.findById(orderId2).orElseThrow();

        assertEquals(OrderSummary.OrderStatus.CONFIRMED, order1.getStatus());
        assertEquals(OrderSummary.OrderStatus.CREATED, order2.getStatus());
        assertEquals("CUST-FS-003", order1.getCustomerId());
        assertEquals("CUST-FS-003", order2.getCustomerId());
    }

    // ============================================================================
    // SCENARIO 4: Error Handling
    // ============================================================================

    @Test
    @DisplayName("Handle invalid operations gracefully")
    void testErrorHandling_InvalidOperations() {
        String orderId = createOrder("CUST-FS-004", "Error Test", "error@example.com", "Error St");

        // Try to confirm order without items (should fail)
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/" + orderId + "/confirm", null, String.class);

        // Should return error (409 Conflict or 400 Bad Request)
        assertTrue(response.getStatusCode().is4xxClientError(),
                "Should return 4xx error for invalid operation");

        // Order should still be in CREATED status
        OrderSummary order = orderSummaryRepository.findById(orderId).orElseThrow();
        assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
    }

    @Test
    @DisplayName("Handle non-existent order gracefully")
    void testErrorHandling_NonExistentOrder() {
        String nonExistentOrderId = "NON-EXISTENT-ORDER-ID";

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/" + nonExistentOrderId + "/confirm", null, String.class);

        assertTrue(response.getStatusCode().is4xxClientError(),
                "Should return 4xx error for non-existent order");
    }

    // ============================================================================
    // SCENARIO 5: Concurrent Operations
    // ============================================================================

    @Test
    @DisplayName("Multiple users creating orders concurrently")
    void testConcurrentOrderCreation() throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        int numberOfUsers = 5;
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] futures = new CompletableFuture[numberOfUsers];

        // Simulate multiple users creating orders simultaneously
        for (int i = 0; i < numberOfUsers; i++) {
            final int userId = i;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                return createOrder(
                        "CUST-CONCURRENT-" + userId,
                        "User " + userId,
                        "user" + userId + "@example.com",
                        "Address " + userId
                );
            });
        }

        // Wait for all to complete
        CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);

        // Wait for all events to be processed
        waitForEventProcessing(1000); // Longer wait for concurrent operations

        // Verify all orders were created
        assertEquals(numberOfUsers, orderSummaryRepository.count());
    }

    // ============================================================================
    // Helper Methods (Simulate Frontend API Calls)
    // ============================================================================

    private String createOrder(String customerId, String customerName, 
                              String customerEmail, String shippingAddress) {
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setCustomerName(customerName);
        request.setCustomerEmail(customerEmail);
        request.setShippingAddress(shippingAddress);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl, request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private void addItemToOrder(String orderId, String productId, String productName,
                               int quantity, BigDecimal price) {
        OrderController.AddItemRequest request = new OrderController.AddItemRequest();
        request.setProductId(productId);
        request.setProductName(productName);
        request.setQuantity(quantity);
        request.setPrice(price);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl + "/" + orderId + "/items", request, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private void confirmOrder(String orderId) {
        ResponseEntity<Void> response = restTemplate.postForEntity(
                baseUrl + "/" + orderId + "/confirm", null, Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ============================================================================
    // Helper Methods for Async Event Processing
    // ============================================================================

    /**
     * Wait for async event processing to complete.
     * In a real scenario, events are processed asynchronously, so we need to wait
     * for the read model to be updated.
     */
    private void waitForEventProcessing() {
        waitForEventProcessing(200);
    }

    private void waitForEventProcessing(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

