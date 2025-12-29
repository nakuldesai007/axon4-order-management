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

public class OrderItemManagementSteps {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;



    private String orderId;
    private String productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private Exception lastException;

    private BigDecimal expectedTotal;

    @Before
    public void setUp() {
        // Clean up test data before each scenario
        orderSummaryRepository.deleteAll();
        lastException = null;

        expectedTotal = BigDecimal.ZERO;
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
            // Override the order ID for testing purposes
            this.orderId = createdOrderId;
        } catch (Exception e) {
            fail("Failed to create test order: " + e.getMessage());
        }
    }

    @Given("a product with ID {string} and name {string}")
    public void aProductWithIdAndName(String productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    @Given("the product price is {bigdecimal}")
    public void theProductPriceIs(BigDecimal price) {
        this.price = price;
    }

    @Given("the quantity is {int}")
    public void theQuantityIs(int quantity) {
        this.quantity = quantity;
    }

    @When("I add this item to the order")
    public void iAddThisItemToTheOrder() {
        try {
            OrderController.AddItemRequest request = new OrderController.AddItemRequest();
            request.setProductId(productId);
            request.setProductName(productName);
            request.setPrice(price);
            request.setQuantity(quantity);

            CompletableFuture<Void> future = orderController.addItemToOrder(orderId, request);
            future.get();
            

            expectedTotal = expectedTotal.add(price.multiply(BigDecimal.valueOf(quantity)));
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I add another product with ID {string} and name {string}")
    public void iAddAnotherProductWithIdAndName(String productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    @When("I add this item to order {string}")
    public void iAddThisItemToOrder(String targetOrderId) {
        try {
            OrderController.AddItemRequest request = new OrderController.AddItemRequest();
            request.setProductId(productId);
            request.setProductName(productName);
            request.setPrice(price);
            request.setQuantity(quantity);

            CompletableFuture<Void> future = orderController.addItemToOrder(targetOrderId, request);
            future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I remove this item from the order")
    public void iRemoveThisItemFromTheOrder() {
        try {
            CompletableFuture<Void> future = orderController.removeItemFromOrder(orderId, productId);
            future.get();

            expectedTotal = expectedTotal.subtract(price.multiply(BigDecimal.valueOf(quantity)));
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the item should be added successfully")
    public void theItemShouldBeAddedSuccessfully() {
        assertNull(lastException);
    }

    @Then("the order should contain {int} item")
    public void theOrderShouldContainItem(int expectedCount) {
        theOrderShouldContainItems(expectedCount);
    }

    @Then("the order should contain {int} items")
    public void theOrderShouldContainItems(int expectedCount) {
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(expectedCount, order.getItems().size());
    }

    @Then("the order total should be {bigdecimal}")
    public void theOrderTotalShouldBe(BigDecimal expectedTotal) {
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(0, expectedTotal.compareTo(order.getTotalAmount()));
    }

    @Then("the item addition should fail")
    public void theItemAdditionShouldFail() {
        assertNotNull(lastException);
    }

    @Then("the item should be removed successfully")
    public void theItemShouldBeRemovedSuccessfully() {
        assertNull(lastException);
    }

    @Then("the order should not contain the item")
    public void theOrderShouldNotContainTheItem() {
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertTrue(order.getItems().stream()
                .noneMatch(item -> item.getProductId().equals(productId)));
    }

    @Then("the item removal should fail")
    public void theItemRemovalShouldFail() {
        assertNotNull(lastException);
    }

    @Given("the order contains a product with ID {string}")
    public void theOrderContainsAProductWithId(String productId) {
        this.productId = productId;
        // First add the item to the order
        OrderController.AddItemRequest request = new OrderController.AddItemRequest();
        request.setProductId(productId);
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

    @Given("the order does not contain a product with ID {string}")
    public void theOrderDoesNotContainAProductWithId(String productId) {
        this.productId = productId;
    }
} 