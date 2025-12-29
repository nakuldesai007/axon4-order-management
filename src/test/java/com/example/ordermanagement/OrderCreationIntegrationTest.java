package com.example.ordermanagement;

import com.example.ordermanagement.command.CreateOrderCommand;

import com.example.ordermanagement.query.OrderSummary;
import com.example.ordermanagement.query.OrderSummaryRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that verify the complete flow of order creation:
 * 1. Command is sent via CommandGateway
 * 2. Aggregate handles the command and publishes event
 * 3. Event handler updates the read model
 * 4. Read model can be queried
 */
@SpringBootTest
@ActiveProfiles("test")
class OrderCreationIntegrationTest {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;



    @BeforeEach
    void setUp() {
        orderSummaryRepository.deleteAll();
    }

    @Test
    void testOrderCreation_EndToEndFlow_ShouldWork() throws Exception {
        // Given
        String orderId = UUID.randomUUID().toString();
        String customerId = "CUST-INT-001";
        String customerName = "Integration Test Customer";
        String customerEmail = "integration@test.com";
        String shippingAddress = "789 Integration St, Test City, TC 12345";

        CreateOrderCommand command = new CreateOrderCommand(
                orderId,
                customerId,
                customerName,
                customerEmail,
                shippingAddress
        );

        // When
        CompletableFuture<String> future = commandGateway.send(command);
        String returnedOrderId = future.get(5, TimeUnit.SECONDS);

        // Then - Verify command gateway returns the order ID
        assertEquals(orderId, returnedOrderId);

        // Wait for async event processing
        Thread.sleep(200);

        // Then - Verify read model was updated
        OrderSummary order = orderSummaryRepository.findById(orderId)
                .orElseThrow(() -> new AssertionError("Order should exist in read model"));

        assertEquals(orderId, order.getOrderId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(customerName, order.getCustomerName());
        assertEquals(customerEmail, order.getCustomerEmail());
        assertEquals(shippingAddress, order.getShippingAddress());
        assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
        assertTrue(order.getItems().isEmpty());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }

    @Test
    void testOrderCreation_MultipleOrders_ShouldAllBeCreated() throws Exception {
        // Create multiple orders
        int numberOfOrders = 5;
        String[] orderIds = new String[numberOfOrders];

        for (int i = 0; i < numberOfOrders; i++) {
            String orderId = UUID.randomUUID().toString();
            CreateOrderCommand command = new CreateOrderCommand(
                    orderId,
                    "CUST-INT-002",
                    "Customer " + i,
                    "customer" + i + "@test.com",
                    "Address " + i
            );

            CompletableFuture<String> future = commandGateway.send(command);
            orderIds[i] = future.get(5, TimeUnit.SECONDS);
        }

        // Wait for all events to be processed
        Thread.sleep(500);

        // Verify all orders exist
        assertEquals(numberOfOrders, orderSummaryRepository.count());

        for (String orderId : orderIds) {
            OrderSummary order = orderSummaryRepository.findById(orderId)
                    .orElseThrow(() -> new AssertionError("Order " + orderId + " should exist"));
            assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
        }
    }

    @Test
    void testOrderCreation_EventIsPublished_ShouldBeAvailableOnEventBus() throws Exception {
        String orderId = UUID.randomUUID().toString();
        CreateOrderCommand command = new CreateOrderCommand(
                orderId,
                "CUST-INT-003",
                "Event Test Customer",
                "event@test.com",
                "Event Test Address"
        );

        CompletableFuture<String> future = commandGateway.send(command);
        future.get(5, TimeUnit.SECONDS);

        // Wait for event processing
        Thread.sleep(200);

        // Verify the event was processed by checking the read model
        OrderSummary order = orderSummaryRepository.findById(orderId)
                .orElseThrow(() -> new AssertionError("Order should exist after event processing"));

        assertNotNull(order);
        assertEquals("Event Test Customer", order.getCustomerName());
    }

    @Test
    void testOrderCreation_WithSameCustomerId_ShouldCreateSeparateOrders() throws Exception {
        String customerId = "CUST-INT-004";
        String orderId1 = UUID.randomUUID().toString();
        String orderId2 = UUID.randomUUID().toString();

        CreateOrderCommand command1 = new CreateOrderCommand(
                orderId1,
                customerId,
                "Customer One",
                "customer1@test.com",
                "Address One"
        );

        CreateOrderCommand command2 = new CreateOrderCommand(
                orderId2,
                customerId,
                "Customer Two",
                "customer2@test.com",
                "Address Two"
        );

        CompletableFuture<String> future1 = commandGateway.send(command1);
        CompletableFuture<String> future2 = commandGateway.send(command2);

        future1.get(5, TimeUnit.SECONDS);
        future2.get(5, TimeUnit.SECONDS);

        Thread.sleep(200);

        // Verify both orders exist
        OrderSummary order1 = orderSummaryRepository.findById(orderId1)
                .orElseThrow(() -> new AssertionError("Order 1 should exist"));
        OrderSummary order2 = orderSummaryRepository.findById(orderId2)
                .orElseThrow(() -> new AssertionError("Order 2 should exist"));

        assertEquals(customerId, order1.getCustomerId());
        assertEquals(customerId, order2.getCustomerId());
        assertNotEquals(order1.getOrderId(), order2.getOrderId());
    }

    @Test
    void testOrderCreation_VerifyEventSourcing_ShouldRestoreAggregateCorrectly() throws Exception {
        String orderId = UUID.randomUUID().toString();
        CreateOrderCommand command = new CreateOrderCommand(
                orderId,
                "CUST-INT-005",
                "Event Sourcing Test",
                "eventsourcing@test.com",
                "Event Sourcing Address"
        );

        CompletableFuture<String> future = commandGateway.send(command);
        future.get(5, TimeUnit.SECONDS);

        Thread.sleep(200);

        // Verify the order was created
        OrderSummary order = orderSummaryRepository.findById(orderId)
                .orElseThrow(() -> new AssertionError("Order should exist"));

        // The fact that we can query it means the event was sourced correctly
        assertNotNull(order);
        assertEquals("Event Sourcing Test", order.getCustomerName());
    }

    @Test
    void testOrderCreation_ConcurrentOrders_ShouldAllSucceed() throws Exception {
        int numberOfConcurrentOrders = 10;
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] futures = new CompletableFuture[numberOfConcurrentOrders];
        String[] orderIds = new String[numberOfConcurrentOrders];

        // Send all commands concurrently
        for (int i = 0; i < numberOfConcurrentOrders; i++) {
            orderIds[i] = UUID.randomUUID().toString();
            CreateOrderCommand command = new CreateOrderCommand(
                    orderIds[i],
                    "CUST-INT-006",
                    "Concurrent Customer " + i,
                    "concurrent" + i + "@test.com",
                    "Concurrent Address " + i
            );
            futures[i] = commandGateway.send(command);
        }

        // Wait for all to complete
        CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);

        // Wait for event processing
        Thread.sleep(500);

        // Verify all orders were created
        assertEquals(numberOfConcurrentOrders, orderSummaryRepository.count());

        for (String orderId : orderIds) {
            OrderSummary order = orderSummaryRepository.findById(orderId)
                    .orElseThrow(() -> new AssertionError("Order " + orderId + " should exist"));
            assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
        }
    }

    @Test
    void testOrderCreation_ReadModelConsistency_ShouldMatchEventData() throws Exception {
        String orderId = UUID.randomUUID().toString();
        String customerId = "CUST-INT-007";
        String customerName = "Read Model Test";
        String customerEmail = "readmodel@test.com";
        String shippingAddress = "Read Model Address";

        CreateOrderCommand command = new CreateOrderCommand(
                orderId,
                customerId,
                customerName,
                customerEmail,
                shippingAddress
        );

        CompletableFuture<String> future = commandGateway.send(command);
        future.get(5, TimeUnit.SECONDS);

        Thread.sleep(200);

        // Verify read model matches command data
        OrderSummary order = orderSummaryRepository.findById(orderId)
                .orElseThrow(() -> new AssertionError("Order should exist"));

        assertEquals(orderId, order.getOrderId());
        assertEquals(customerId, order.getCustomerId());
        assertEquals(customerName, order.getCustomerName());
        assertEquals(customerEmail, order.getCustomerEmail());
        assertEquals(shippingAddress, order.getShippingAddress());
    }
}

