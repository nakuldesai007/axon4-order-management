package com.example.ordermanagement.cucumber;

import com.example.ordermanagement.controller.OrderController;
import com.example.ordermanagement.query.OrderSummary;
import com.example.ordermanagement.query.OrderSummaryRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class OrderStatusWorkflowSteps {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;



    private String orderId;


    private Exception lastException;

    @Before
    public void setUp() {
        // Clean up test data before each scenario
        orderSummaryRepository.deleteAll();
        lastException = null;
    }

    @Given("an order exists with ID {string} for customer {string}")
    public void anOrderExistsWithIdForCustomer(String orderId, String customerId) {
        this.orderId = orderId;
        
        // Create the order
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setShippingAddress("123 Test St");

        try {
            CompletableFuture<String> future = orderController.createOrder(request);
            String createdOrderId = future.get();
            this.orderId = createdOrderId;
        } catch (Exception e) {
            fail("Failed to create test order: " + e.getMessage());
        }
    }

    @Given("the order contains items")
    public void theOrderContainsItems() {
        // Add an item to the order
        OrderController.AddItemRequest request = new OrderController.AddItemRequest();
        request.setProductId("PROD-001");
        request.setProductName("Test Product");
        request.setPrice(BigDecimal.valueOf(100.00));
        request.setQuantity(1);

        try {
            CompletableFuture<Void> future = orderController.addItemToOrder(orderId, request);
            future.get();
        } catch (Exception e) {
            fail("Failed to add test item: " + e.getMessage());
        }
    }

    @Given("an order with status {string}")
    public void anOrderWithStatus(String status) {
        // Create order and set it to the specified status
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId("CUST-TEST");
        request.setCustomerName("Test Customer");
        request.setCustomerEmail("test@example.com");
        request.setShippingAddress("123 Test St");

        try {
            CompletableFuture<String> future = orderController.createOrder(request);
            orderId = future.get();

            // Add an item
            OrderController.AddItemRequest itemRequest = new OrderController.AddItemRequest();
            itemRequest.setProductId("PROD-001");
            itemRequest.setProductName("Test Product");
            itemRequest.setPrice(BigDecimal.valueOf(100.00));
            itemRequest.setQuantity(1);
            orderController.addItemToOrder(orderId, itemRequest).get();

            // Set status based on the required status
            switch (status) {
                case "CONFIRMED":
                    orderController.confirmOrder(orderId).get();
                    break;
                case "PROCESSED":
                    orderController.confirmOrder(orderId).get();
                    orderController.processOrder(orderId).get();
                    break;
                case "SHIPPED":
                    orderController.confirmOrder(orderId).get();
                    orderController.processOrder(orderId).get();
                    OrderController.ShipOrderRequest shipRequest = new OrderController.ShipOrderRequest();
                    shipRequest.setTrackingNumber("TRK123456789");
                    orderController.shipOrder(orderId, shipRequest).get();
                    break;
                case "CANCELLED":
                    OrderController.CancelOrderRequest cancelRequest = new OrderController.CancelOrderRequest();
                    cancelRequest.setReason("Test cancellation");
                    orderController.cancelOrder(orderId, cancelRequest).get();
                    break;
            }
        } catch (Exception e) {
            fail("Failed to setup order with status " + status + ": " + e.getMessage());
        }
    }

    @When("I confirm the order")
    public void iConfirmTheOrder() {
        try {
            CompletableFuture<Void> future = orderController.confirmOrder(orderId);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I process the order")
    public void iProcessTheOrder() {
        try {
            CompletableFuture<Void> future = orderController.processOrder(orderId);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I ship the order with tracking number {string}")
    public void iShipTheOrderWithTrackingNumber(String trackingNumber) {

        try {
            OrderController.ShipOrderRequest request = new OrderController.ShipOrderRequest();
            request.setTrackingNumber(trackingNumber);
            CompletableFuture<Void> future = orderController.shipOrder(orderId, request);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I cancel the order with reason {string}")
    public void iCancelTheOrderWithReason(String reason) {

        try {
            OrderController.CancelOrderRequest request = new OrderController.CancelOrderRequest();
            request.setReason(reason);
            CompletableFuture<Void> future = orderController.cancelOrder(orderId, request);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I attempt to cancel the order")
    public void iAttemptToCancelTheOrder() {
        try {
            OrderController.CancelOrderRequest request = new OrderController.CancelOrderRequest();
            request.setReason("Test cancellation");
            CompletableFuture<Void> future = orderController.cancelOrder(orderId, request);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I attempt to confirm the order again")
    public void iAttemptToConfirmTheOrderAgain() {
        try {
            CompletableFuture<Void> future = orderController.confirmOrder(orderId);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I attempt to process the order")
    public void iAttemptToProcessTheOrder() {
        try {
            CompletableFuture<Void> future = orderController.processOrder(orderId);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I attempt to ship the order")
    public void iAttemptToShipTheOrder() {
        try {
            OrderController.ShipOrderRequest request = new OrderController.ShipOrderRequest();
            request.setTrackingNumber("TRK123456789");
            CompletableFuture<Void> future = orderController.shipOrder(orderId, request);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I attempt to ship the order without tracking number")
    public void iAttemptToShipTheOrderWithoutTrackingNumber() {
        try {
            OrderController.ShipOrderRequest request = new OrderController.ShipOrderRequest();
            request.setTrackingNumber("");
            CompletableFuture<Void> future = orderController.shipOrder(orderId, request);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the order status should be {string}")
    public void theOrderStatusShouldBe(String expectedStatus) {
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(OrderSummary.OrderStatus.valueOf(expectedStatus), order.getStatus());
    }

    @Then("the order should have tracking number {string}")
    public void theOrderShouldHaveTrackingNumber(String expectedTrackingNumber) {
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(expectedTrackingNumber, order.getTrackingNumber());
    }

    @Then("the cancellation reason should be {string}")
    public void theCancellationReasonShouldBe(String expectedReason) {
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(expectedReason, order.getCancellationReason());
    }

    @Then("the cancellation should fail")
    public void theCancellationShouldFail() {
        assertNotNull(lastException);
    }

    @Then("the confirmation should fail")
    public void theConfirmationShouldFail() {
        assertNotNull(lastException);
    }

    @Then("the processing should fail")
    public void theProcessingShouldFail() {
        assertNotNull(lastException);
    }

    @Then("the shipment should fail")
    public void theShipmentShouldFail() {
        assertNotNull(lastException);
    }
} 