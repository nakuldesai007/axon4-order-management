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
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class OrderQuerySteps {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;








    private ResponseEntity<OrderSummary> orderResponse;
    private List<OrderSummary> ordersResponse;
    private long countResponse;
    private ResponseEntity<BigDecimal> averageResponse;


    @Before
    public void setUp() {
        // Clean up test data before each scenario
        orderSummaryRepository.deleteAll();

        orderResponse = null;
        ordersResponse = null;
        countResponse = 0;
        averageResponse = null;
    }

    @Given("the order management system is running")
    public void theOrderManagementSystemIsRunning() {
        assertNotNull(orderController);
        assertNotNull(orderSummaryRepository);

    }

    @Given("multiple orders exist in the system")
    public void multipleOrdersExistInTheSystem() {
        // Create multiple test orders
        createTestOrder("CUST-001", "John Smith", "CREATED");
        createTestOrder("CUST-002", "Jane Doe", "CONFIRMED");
        createTestOrder("CUST-003", "Bob Johnson", "PROCESSED");
    }

    @Given("an order with ID {string} exists")
    public void anOrderWithIdExists(String orderId) {

        createTestOrder("CUST-TEST", "Test Customer", "CREATED", orderId);
    }

    @Given("no order with ID {string} exists")
    public void noOrderWithIdExists(String orderId) {

        // Ensure the order doesn't exist by not creating it
    }

    @Given("a customer with ID {string} has multiple orders")
    public void aCustomerWithIdHasMultipleOrders(String customerId) {

        createTestOrder(customerId, "Test Customer 1", "CREATED");
        createTestOrder(customerId, "Test Customer 2", "CONFIRMED");
    }

    @Given("there are orders with status {string}")
    public void thereAreOrdersWithStatus(String status) {

        createTestOrder("CUST-STATUS-1", "Status Customer 1", status);
        createTestOrder("CUST-STATUS-2", "Status Customer 2", status);
    }

    @Given("there are orders for customer {string}")
    public void thereAreOrdersForCustomer(String customerName) {

        createTestOrder("CUST-NAME-1", customerName + " First", "CREATED");
        createTestOrder("CUST-NAME-2", customerName + " Second", "CONFIRMED");
    }

    @Given("there are orders with total amount greater than {bigdecimal}")
    public void thereAreOrdersWithTotalAmountGreaterThan(BigDecimal minAmount) {

        createTestOrderWithAmount("CUST-AMOUNT-1", "Amount Customer 1", minAmount.add(BigDecimal.valueOf(50)));
        createTestOrderWithAmount("CUST-AMOUNT-2", "Amount Customer 2", minAmount.add(BigDecimal.valueOf(100)));
    }

    @Given("there are no orders with status {string}")
    public void thereAreNoOrdersWithStatus(String status) {

        // Don't create any orders with this status
    }

    @When("I retrieve the order by ID {string}")
    public void iRetrieveTheOrderById(String orderId) {
        try {
            orderResponse = orderController.getOrder(orderId);
        } catch (Exception e) {
            fail("Failed: " + e.getMessage());
        }
    }

    @When("I retrieve all orders")
    public void iRetrieveAllOrders() {
        try {
            ordersResponse = orderController.getAllOrders();
        } catch (Exception e) {
            fail("Failed: " + e.getMessage());
        }
    }

    @When("I retrieve orders for customer {string}")
    public void iRetrieveOrdersForCustomer(String customerId) {
        try {
            ordersResponse = orderController.getOrdersByCustomer(customerId);
        } catch (Exception e) {
            fail("Failed: " + e.getMessage());
        }
    }

    @When("I retrieve orders with status {string}")
    public void iRetrieveOrdersWithStatus(String status) {
        try {
            ordersResponse = orderController.getOrdersByStatus(status);
        } catch (Exception e) {
            fail("Failed: " + e.getMessage());
        }
    }

    @When("I search orders by customer name {string}")
    public void iSearchOrdersByCustomerName(String customerName) {
        try {
            ordersResponse = orderController.searchOrdersByCustomerName(customerName);
        } catch (Exception e) {
            fail("Failed: " + e.getMessage());
        }
    }

    @When("I retrieve orders with minimum amount {bigdecimal}")
    public void iRetrieveOrdersWithMinimumAmount(BigDecimal minAmount) {
        try {
            ordersResponse = orderController.getOrdersByMinAmount(minAmount);
        } catch (Exception e) {
            fail("Failed: " + e.getMessage());
        }
    }

    @When("I get the count of orders with status {string}")
    public void iGetTheCountOfOrdersWithStatus(String status) {
        try {
            countResponse = orderController.getOrderCountByStatus(status);
        } catch (Exception e) {
            fail("Failed: " + e.getMessage());
        }
    }

    @When("I get the average order value for status {string}")
    public void iGetTheAverageOrderValueForStatus(String status) {
        try {
            averageResponse = orderController.getAverageOrderValueByStatus(status);
        } catch (Exception e) {
            fail("Failed: " + e.getMessage());
        }
    }

    @Then("the order should be returned")
    public void theOrderShouldBeReturned() {
        assertNotNull(orderResponse);
        assertTrue(orderResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(orderResponse.getBody());
    }

    @Then("the order ID should be {string}")
    public void theOrderIdShouldBe(String expectedOrderId) {
        assertEquals(expectedOrderId, orderResponse.getBody().getOrderId());
    }

    @Then("no order should be returned")
    public void noOrderShouldBeReturned() {
        assertNotNull(orderResponse);
        assertTrue(orderResponse.getStatusCode().is4xxClientError());
    }

    @Then("a {int} error should be returned")
    public void aErrorShouldBeReturned(int errorCode) {
        assertNotNull(orderResponse);
        assertEquals(errorCode, orderResponse.getStatusCode().value());
    }

    @Then("a list of orders should be returned")
    public void aListOfOrdersShouldBeReturned() {
        assertNotNull(ordersResponse);
        assertFalse(ordersResponse.isEmpty());
    }

    @Then("the list should not be empty")
    public void theListShouldNotBeEmpty() {
        assertFalse(ordersResponse.isEmpty());
    }

    @Then("all orders should belong to customer {string}")
    public void allOrdersShouldBelongToCustomer(String expectedCustomerId) {
        assertTrue(ordersResponse.stream()
                .allMatch(order -> expectedCustomerId.equals(order.getCustomerId())));
    }

    @Then("all orders should have status {string}")
    public void allOrdersShouldHaveStatus(String expectedStatus) {
        assertTrue(ordersResponse.stream()
                .allMatch(order -> OrderSummary.OrderStatus.valueOf(expectedStatus).equals(order.getStatus())));
    }

    @Then("all orders should have customer names containing {string}")
    public void allOrdersShouldHaveCustomerNamesContaining(String expectedName) {
        assertTrue(ordersResponse.stream()
                .allMatch(order -> order.getCustomerName().contains(expectedName)));
    }

    @Then("all orders should have total amount greater than or equal to {bigdecimal}")
    public void allOrdersShouldHaveTotalAmountGreaterThanOrEqualTo(BigDecimal expectedMinAmount) {
        assertTrue(ordersResponse.stream()
                .allMatch(order -> order.getTotalAmount().compareTo(expectedMinAmount) >= 0));
    }

    @Then("a count should be returned")
    public void aCountShouldBeReturned() {
        assertTrue(countResponse >= 0);
    }

    @Then("the count should be greater than {int}")
    public void theCountShouldBeGreaterThan(int expectedMinCount) {
        assertTrue(countResponse > expectedMinCount);
    }

    @Then("an average value should be returned")
    public void anAverageValueShouldBeReturned() {
        assertNotNull(averageResponse);
        assertTrue(averageResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(averageResponse.getBody());
    }

    @Then("the average should be greater than {int}")
    public void theAverageShouldBeGreaterThan(int expectedMinAverage) {
        assertTrue(averageResponse.getBody().compareTo(BigDecimal.valueOf(expectedMinAverage)) > 0);
    }

    @Then("no average value should be returned")
    public void noAverageValueShouldBeReturned() {
        assertNotNull(averageResponse);
        assertTrue(averageResponse.getStatusCode().is4xxClientError());
    }

    private void createTestOrder(String customerId, String customerName, String status) {
        createTestOrder(customerId, customerName, status, null);
    }

    private void createTestOrder(String customerId, String customerName, String status, String specificOrderId) {
        try {
            OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
            request.setCustomerId(customerId);
            request.setCustomerName(customerName);
            request.setCustomerEmail("test@example.com");
            request.setShippingAddress("123 Test St");

            CompletableFuture<String> future = orderController.createOrder(request);
            String orderId = future.get();

            // Add an item
            OrderController.AddItemRequest itemRequest = new OrderController.AddItemRequest();
            itemRequest.setProductId("PROD-001");
            itemRequest.setProductName("Test Product");
            itemRequest.setPrice(BigDecimal.valueOf(100.00));
            itemRequest.setQuantity(1);
            orderController.addItemToOrder(orderId, itemRequest).get();

            // Set status
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
            }
        } catch (Exception e) {
            fail("Failed to create test order: " + e.getMessage());
        }
    }

    private void createTestOrderWithAmount(String customerId, String customerName, BigDecimal amount) {
        try {
            OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
            request.setCustomerId(customerId);
            request.setCustomerName(customerName);
            request.setCustomerEmail("test@example.com");
            request.setShippingAddress("123 Test St");

            CompletableFuture<String> future = orderController.createOrder(request);
            String orderId = future.get();

            // Add an item with the specified amount
            OrderController.AddItemRequest itemRequest = new OrderController.AddItemRequest();
            itemRequest.setProductId("PROD-001");
            itemRequest.setProductName("Test Product");
            itemRequest.setPrice(amount);
            itemRequest.setQuantity(1);
            orderController.addItemToOrder(orderId, itemRequest).get();
        } catch (Exception e) {
            fail("Failed to create test order with amount: " + e.getMessage());
        }
    }
} 