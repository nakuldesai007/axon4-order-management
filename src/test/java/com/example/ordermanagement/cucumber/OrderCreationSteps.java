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



import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class OrderCreationSteps {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;

    @Autowired
    private CommandGateway commandGateway;

    private String customerId;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private String createdOrderId;
    private List<String> createdOrderIds;
    private Exception lastException;

    @Before
    public void setUp() {
        // Clean up test data before each scenario
        orderSummaryRepository.deleteAll();
        lastException = null;
        createdOrderId = null;
        createdOrderIds = null;
    }

    @Given("the order management system is running")
    public void theOrderManagementSystemIsRunning() {
        assertNotNull(orderController);
        assertNotNull(orderSummaryRepository);
        assertNotNull(commandGateway);
    }

    @Given("a customer with ID {string} and name {string}")
    public void aCustomerWithIdAndName(String customerId, String customerName) {
        this.customerId = customerId;
        this.customerName = customerName;
    }

    @Given("the customer email is {string}")
    public void theCustomerEmailIs(String email) {
        this.customerEmail = email;
    }

    @Given("the shipping address is {string}")
    public void theShippingAddressIs(String address) {
        this.shippingAddress = address;
    }

    @When("I create a new order for this customer")
    public void iCreateANewOrderForThisCustomer() {
        try {
            OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
            request.setCustomerId(customerId);
            request.setCustomerName(customerName);
            request.setCustomerEmail(customerEmail);
            request.setShippingAddress(shippingAddress);

            CompletableFuture<String> future = orderController.createOrder(request);
            createdOrderId = future.get();
        } catch (Exception e) {
            lastException = e;
        }
    }

    @When("I create another new order for this customer")
    public void iCreateAnotherNewOrderForThisCustomer() {
        try {
            OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
            request.setCustomerId(customerId);
            request.setCustomerName(customerName);
            request.setCustomerEmail(customerEmail);
            request.setShippingAddress(shippingAddress);

            CompletableFuture<String> future = orderController.createOrder(request);
            String secondOrderId = future.get();
            createdOrderIds = List.of(createdOrderId, secondOrderId);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the order should be created successfully")
    public void theOrderShouldBeCreatedSuccessfully() {
        assertNotNull(createdOrderId);
        assertFalse(createdOrderId.isEmpty());
    }

    @Then("the order status should be {string}")
    public void theOrderStatusShouldBe(String expectedStatus) {
        OrderSummary order = orderSummaryRepository.findById(createdOrderId).orElse(null);
        assertNotNull(order);
        assertEquals(OrderSummary.OrderStatus.valueOf(expectedStatus), order.getStatus());
    }

    @Then("the order should have no items")
    public void theOrderShouldHaveNoItems() {
        OrderSummary order = orderSummaryRepository.findById(createdOrderId).orElse(null);
        assertNotNull(order);
        assertTrue(order.getItems().isEmpty());
    }

    @Then("the order creation should fail")
    public void theOrderCreationShouldFail() {
        assertNotNull(lastException);
    }

    @Then("an error message should be returned")
    public void anErrorMessageShouldBeReturned() {
        assertNotNull(lastException);
    }

    @Then("both orders should be created successfully")
    public void bothOrdersShouldBeCreatedSuccessfully() {
        assertNotNull(createdOrderIds);
        assertEquals(2, createdOrderIds.size());
        assertFalse(createdOrderIds.get(0).isEmpty());
        assertFalse(createdOrderIds.get(1).isEmpty());
    }

    @Then("both orders should have different IDs")
    public void bothOrdersShouldHaveDifferentIds() {
        assertNotEquals(createdOrderIds.get(0), createdOrderIds.get(1));
    }

    @Then("both orders should have status {string}")
    public void bothOrdersShouldHaveStatus(String expectedStatus) {
        for (String orderId : createdOrderIds) {
            OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
            assertNotNull(order);
            assertEquals(OrderSummary.OrderStatus.valueOf(expectedStatus), order.getStatus());
        }
    }
} 