package com.example.ordermanagement.excel;

import com.example.ordermanagement.excel.CSVTestCaseReader;
import com.example.ordermanagement.excel.ExcelTestCaseReader;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that execute test cases from Excel files.
 * Tests multi-day order lifecycle scenarios where event sourcing maintains state.
 * 
 * Test cases are defined in: src/test/resources/test-cases/multi-day-order-lifecycle.xlsx
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Multi-Day Order Lifecycle Integration Tests (Excel-based)")
class MultiDayOrderLifecycleExcelTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;

    @Autowired
    private OrderController orderController;

    private String baseUrl;
    private Map<String, String> testContext; // Store order IDs and other test data

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/orders";
        orderSummaryRepository.deleteAll();
        testContext = new HashMap<>();
    }

    @Test
    @DisplayName("Execute all test cases from Excel/CSV file")
    void testExecuteAllTestCasesFromExcel() {
        // Try to read from Excel first, fallback to CSV
        List<ExcelTestCaseReader.TestCase> allTestCases;
        try {
            allTestCases = ExcelTestCaseReader.readTestCases("test-cases/multi-day-order-lifecycle.xlsx");
        } catch (Exception e) {
            // Fallback to CSV if Excel is not available
            System.out.println("Excel file not found, using CSV: " + e.getMessage());
            allTestCases = CSVTestCaseReader.readTestCasesFromCSV("test-cases/multi-day-order-lifecycle.csv");
        }
        
        // Filter out Unit tests (edge cases) - only run Day-1, Day-2, Day-3 integration tests
        allTestCases = allTestCases.stream()
                .filter(tc -> tc.getDay() != null && 
                             (tc.getDay().equals("Day-1") || 
                              tc.getDay().equals("Day-2") || 
                              tc.getDay().equals("Day-3")))
                .toList();
        
        assertFalse(allTestCases.isEmpty(), "Should have test cases in Excel file");
        
        // Group by test case ID and day
        Map<String, Map<String, List<ExcelTestCaseReader.TestCase>>> grouped = 
            ExcelTestCaseReader.groupByTestCaseAndDay(allTestCases);
        
        // Execute each test case scenario
        for (Map.Entry<String, Map<String, List<ExcelTestCaseReader.TestCase>>> testCaseEntry : grouped.entrySet()) {
            String testCaseId = testCaseEntry.getKey();
            Map<String, List<ExcelTestCaseReader.TestCase>> days = testCaseEntry.getValue();
            
            System.out.println("\n=== Executing Test Case: " + testCaseId + " ===");
            
            // Execute steps for each day in order
            List<String> sortedDays = new ArrayList<>(days.keySet());
            sortedDays.sort(Comparator.naturalOrder());
            
            for (String day : sortedDays) {
                System.out.println("\n--- " + day + " ---");
                List<ExcelTestCaseReader.TestCase> steps = days.get(day);
                
                for (ExcelTestCaseReader.TestCase step : steps) {
                    executeStep(step, testCaseId, day);
                }
                
                // Wait a bit for event processing between days
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Test
    @DisplayName("Day-1: Order Creation and Item Management")
    void testDay1_OrderCreationAndItemManagement() {
        executeTestCasesForDay("Day-1");
    }

    @Test
    @DisplayName("Day-2: Order Confirmation and Processing")
    void testDay2_OrderConfirmationAndProcessing() {
        // First execute Day-1 to set up orders
        executeTestCasesForDay("Day-1");
        
        // Then execute Day-2
        executeTestCasesForDay("Day-2");
    }

    @Test
    @DisplayName("Day-3: Order Shipping and Delivery")
    void testDay3_OrderShippingAndDelivery() {
        // Execute Day-1 and Day-2 first
        executeTestCasesForDay("Day-1");
        executeTestCasesForDay("Day-2");
        
        // Then execute Day-3
        executeTestCasesForDay("Day-3");
    }

    @Test
    @DisplayName("Full Lifecycle: Day-1 to Day-3 Complete Flow")
    void testFullLifecycle_Day1ToDay3() {
        List<ExcelTestCaseReader.TestCase> allTestCases = readTestCases();
        
        Map<String, Map<String, List<ExcelTestCaseReader.TestCase>>> grouped = 
            ExcelTestCaseReader.groupByTestCaseAndDay(allTestCases);
        
        // Verify we have test cases before accessing
        assertFalse(grouped.isEmpty(), "Should have at least one test case after filtering");
        
        // Execute complete lifecycle for first test case
        String firstTestCaseId = grouped.keySet().iterator().next();
        Map<String, List<ExcelTestCaseReader.TestCase>> days = grouped.get(firstTestCaseId);
        
        List<String> sortedDays = Arrays.asList("Day-1", "Day-2", "Day-3");
        
        for (String day : sortedDays) {
            if (days.containsKey(day)) {
                System.out.println("\n=== " + day + " ===");
                for (ExcelTestCaseReader.TestCase step : days.get(day)) {
                    executeStep(step, firstTestCaseId, day);
                }
                
                // Verify state after each day
                verifyOrderStateAfterDay(day);
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private List<ExcelTestCaseReader.TestCase> readTestCases() {
        List<ExcelTestCaseReader.TestCase> allTestCases;
        try {
            allTestCases = ExcelTestCaseReader.readTestCases("test-cases/multi-day-order-lifecycle.xlsx");
        } catch (Exception e) {
            // Fallback to CSV
            allTestCases = CSVTestCaseReader.readTestCasesFromCSV("test-cases/multi-day-order-lifecycle.csv");
        }
        
        // Filter out Unit tests (edge cases) - only run Day-1, Day-2, Day-3 integration tests
        return allTestCases.stream()
                .filter(tc -> tc.getDay() != null && 
                             (tc.getDay().equals("Day-1") || 
                              tc.getDay().equals("Day-2") || 
                              tc.getDay().equals("Day-3")))
                .toList();
    }

    private void executeTestCasesForDay(String day) {
        List<ExcelTestCaseReader.TestCase> allTestCases = readTestCases();
        
        List<ExcelTestCaseReader.TestCase> dayTestCases = allTestCases.stream()
            .filter(tc -> tc.isEnabled() && day.equals(tc.getDay()))
            .sorted(Comparator.comparingInt(ExcelTestCaseReader.TestCase::getStepNumber))
            .toList();
        
        for (ExcelTestCaseReader.TestCase testCase : dayTestCases) {
            executeStep(testCase, testCase.getTestCaseId(), day);
        }
    }

    private void executeStep(ExcelTestCaseReader.TestCase step, String testCaseId, String day) {
        System.out.println("  Step " + step.getStepNumber() + ": " + step.getAction());
        
        try {
            switch (step.getAction().toUpperCase()) {
                case "CREATE_ORDER":
                    executeCreateOrder(step);
                    break;
                case "ADD_ITEM":
                    executeAddItem(step);
                    break;
                case "REMOVE_ITEM":
                    executeRemoveItem(step);
                    break;
                case "CONFIRM_ORDER":
                    executeConfirmOrder(step);
                    break;
                case "PROCESS_ORDER":
                    executeProcessOrder(step);
                    break;
                case "SHIP_ORDER":
                    executeShipOrder(step);
                    break;
                case "CANCEL_ORDER":
                    executeCancelOrder(step);
                    break;
                case "VERIFY_ORDER":
                    executeVerifyOrder(step);
                    break;
                default:
                    System.out.println("    Unknown action: " + step.getAction());
            }
        } catch (Exception e) {
            fail("Step " + step.getStepNumber() + " failed: " + e.getMessage(), e);
        }
    }

    private void executeCreateOrder(ExcelTestCaseReader.TestCase step) {
        Map<String, String> params = step.getParameters();
        
        OrderController.CreateOrderRequest request = new OrderController.CreateOrderRequest();
        request.setCustomerId(params.getOrDefault("customerId", "CUST-001"));
        request.setCustomerName(params.getOrDefault("customerName", "Test Customer"));
        request.setCustomerEmail(params.getOrDefault("customerEmail", "test@example.com"));
        request.setShippingAddress(params.getOrDefault("shippingAddress", "123 Test St"));
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl, request, String.class);
        
        assertEquals(200, response.getStatusCodeValue());
        String orderId = response.getBody();
        assertNotNull(orderId);
        
        // Store order ID in context for later steps
        String contextKey = params.getOrDefault("contextKey", "orderId");
        testContext.put(contextKey, orderId);
        
        // Wait for event processing
        waitForEventProcessing();
        
        // Verify order was created
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order, "Order should exist after creation");
        assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
        
        System.out.println("    ✓ Order created: " + orderId);
    }

    private void executeAddItem(ExcelTestCaseReader.TestCase step) {
        Map<String, String> params = step.getParameters();
        String orderId = getOrderIdFromContext(params);
        
        OrderController.AddItemRequest request = new OrderController.AddItemRequest();
        request.setProductId(params.getOrDefault("productId", "PROD-001"));
        request.setProductName(params.getOrDefault("productName", "Test Product"));
        request.setQuantity(Integer.parseInt(params.getOrDefault("quantity", "1")));
        request.setPrice(new BigDecimal(params.getOrDefault("price", "99.99")));
        
        ResponseEntity<Void> response = restTemplate.postForEntity(
            baseUrl + "/" + orderId + "/items", request, Void.class);
        
        assertEquals(200, response.getStatusCodeValue());
        waitForEventProcessing();
        
        // Verify item was added
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertFalse(order.getItems().isEmpty(), "Order should have items");
        
        System.out.println("    ✓ Item added to order");
    }

    private void executeRemoveItem(ExcelTestCaseReader.TestCase step) {
        Map<String, String> params = step.getParameters();
        String orderId = getOrderIdFromContext(params);
        String productId = params.get("productId");
        
        ResponseEntity<Void> response = restTemplate.exchange(
            baseUrl + "/" + orderId + "/items/" + productId,
            HttpMethod.DELETE,
            null,
            Void.class);
        
        assertEquals(200, response.getStatusCodeValue());
        waitForEventProcessing();
        
        System.out.println("    ✓ Item removed from order");
    }

    private void executeConfirmOrder(ExcelTestCaseReader.TestCase step) {
        Map<String, String> params = step.getParameters();
        String orderId = getOrderIdFromContext(params);
        
        ResponseEntity<Void> response = restTemplate.postForEntity(
            baseUrl + "/" + orderId + "/confirm", null, Void.class);
        
        assertEquals(200, response.getStatusCodeValue());
        waitForEventProcessing();
        
        // Verify status
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(OrderSummary.OrderStatus.CONFIRMED, order.getStatus());
        
        System.out.println("    ✓ Order confirmed");
    }

    private void executeProcessOrder(ExcelTestCaseReader.TestCase step) {
        Map<String, String> params = step.getParameters();
        String orderId = getOrderIdFromContext(params);
        
        ResponseEntity<Void> response = restTemplate.postForEntity(
            baseUrl + "/" + orderId + "/process", null, Void.class);
        
        assertEquals(200, response.getStatusCodeValue());
        waitForEventProcessing();
        
        // Verify status
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(OrderSummary.OrderStatus.PROCESSED, order.getStatus());
        
        System.out.println("    ✓ Order processed");
    }

    private void executeShipOrder(ExcelTestCaseReader.TestCase step) {
        Map<String, String> params = step.getParameters();
        String orderId = getOrderIdFromContext(params);
        
        OrderController.ShipOrderRequest request = new OrderController.ShipOrderRequest();
        request.setTrackingNumber(params.getOrDefault("trackingNumber", "TRK" + System.currentTimeMillis()));
        
        ResponseEntity<Void> response = restTemplate.postForEntity(
            baseUrl + "/" + orderId + "/ship", request, Void.class);
        
        assertEquals(200, response.getStatusCodeValue());
        waitForEventProcessing();
        
        // Verify status
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(OrderSummary.OrderStatus.SHIPPED, order.getStatus());
        assertNotNull(order.getTrackingNumber());
        
        System.out.println("    ✓ Order shipped with tracking: " + order.getTrackingNumber());
    }

    private void executeCancelOrder(ExcelTestCaseReader.TestCase step) {
        Map<String, String> params = step.getParameters();
        String orderId = getOrderIdFromContext(params);
        
        OrderController.CancelOrderRequest request = new OrderController.CancelOrderRequest();
        request.setReason(params.getOrDefault("reason", "Test cancellation"));
        
        ResponseEntity<Void> response = restTemplate.postForEntity(
            baseUrl + "/" + orderId + "/cancel", request, Void.class);
        
        assertEquals(200, response.getStatusCodeValue());
        waitForEventProcessing();
        
        // Verify status
        OrderSummary order = orderSummaryRepository.findById(orderId).orElse(null);
        assertNotNull(order);
        assertEquals(OrderSummary.OrderStatus.CANCELLED, order.getStatus());
        
        System.out.println("    ✓ Order cancelled");
    }

    private void executeVerifyOrder(ExcelTestCaseReader.TestCase step) {
        Map<String, String> params = step.getParameters();
        String orderId = getOrderIdFromContext(params);
        
        ResponseEntity<OrderSummary> response = restTemplate.getForEntity(
            baseUrl + "/" + orderId, OrderSummary.class);
        
        assertEquals(200, response.getStatusCodeValue());
        OrderSummary order = response.getBody();
        assertNotNull(order);
        
        // Verify expected status
        if (step.getExpectedStatus() != null && !step.getExpectedStatus().isEmpty()) {
            OrderSummary.OrderStatus expectedStatus = 
                OrderSummary.OrderStatus.valueOf(step.getExpectedStatus());
            assertEquals(expectedStatus, order.getStatus(), 
                "Order status should match expected: " + step.getExpectedStatus());
        }
        
        // Verify expected result (can contain multiple assertions)
        if (step.getExpectedResult() != null && !step.getExpectedResult().isEmpty()) {
            verifyExpectedResult(order, step.getExpectedResult());
        }
        
        System.out.println("    ✓ Order verification passed");
    }

    private void verifyExpectedResult(OrderSummary order, String expectedResult) {
        // Parse expected result (format: "key=value,key2=value2")
        String[] assertions = expectedResult.split(",");
        for (String assertion : assertions) {
            // Use limit 2 to handle values containing equals signs (e.g., key=2+2=4)
            String[] kv = assertion.split("=", 2);
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();
                
                switch (key.toLowerCase()) {
                    case "status":
                        assertEquals(OrderSummary.OrderStatus.valueOf(value), order.getStatus());
                        break;
                    case "itemcount":
                        assertEquals(Integer.parseInt(value), order.getItems().size());
                        break;
                    case "totalamount":
                        assertEquals(new BigDecimal(value), order.getTotalAmount());
                        break;
                    case "customerid":
                        assertEquals(value, order.getCustomerId());
                        break;
                }
            }
        }
    }

    private void verifyOrderStateAfterDay(String day) {
        // Verify that all orders maintain correct state after the day's operations
        List<OrderSummary> allOrders = orderSummaryRepository.findAll();
        
        System.out.println("\n  State after " + day + ":");
        for (OrderSummary order : allOrders) {
            System.out.println("    Order " + order.getOrderId() + 
                " - Status: " + order.getStatus() + 
                ", Items: " + order.getItems().size() +
                ", Total: " + order.getTotalAmount());
        }
        
        // Verify event sourcing maintained state correctly
        assertFalse(allOrders.isEmpty(), "Should have orders after " + day);
    }

    private String getOrderIdFromContext(Map<String, String> params) {
        // Determine the context key to look up:
        // 1. If "orderId" param is provided (e.g., orderId=order1), use that value as the context key
        // 2. If "contextKey" param is provided, use that as the context key
        // 3. Otherwise, default to "orderId" (standard single-order scenario)
        String contextKey;
        if (params.containsKey("orderId")) {
            // When orderId=order1 is in params, use "order1" as the context key
            // This matches how executeCreateOrder stores with contextKey=order1
            contextKey = params.get("orderId");
        } else {
            // Use contextKey parameter if provided, otherwise default to "orderId"
            contextKey = params.getOrDefault("contextKey", "orderId");
        }
        
        String orderId = testContext.get(contextKey);
        if (orderId == null) {
            // Final fallback: if orderId was provided directly as a UUID, use it
            // Only use this fallback if it's a real UUID format (8-4-4-4-12 hex digits)
            // This prevents context keys like "order-1-2" from being incorrectly used as order IDs
            String directOrderId = params.get("orderId");
            if (directOrderId != null && isValidUUID(directOrderId)) {
                // This is a real UUID format, use it as the order ID
                orderId = directOrderId;
            }
        }
        assertNotNull(orderId, "Order ID not found in context with key '" + contextKey + "'. " +
                "Make sure CREATE_ORDER step was executed first. " +
                "For multi-order scenarios, use contextKey in CREATE_ORDER and orderId=<contextKey> in subsequent steps.");
        return orderId;
    }

    /**
     * Validates if a string is a valid UUID format (8-4-4-4-12 hex digits).
     * This prevents context keys like "order-1-2" from being incorrectly matched.
     * 
     * @param value the string to validate
     * @return true if the string matches UUID format, false otherwise
     */
    private boolean isValidUUID(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        // UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        // Where x is a hexadecimal digit [0-9a-fA-F]
        // Pattern: 8 hex digits - 4 hex digits - 4 hex digits - 4 hex digits - 12 hex digits
        return value.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    private void waitForEventProcessing() {
        try {
            Thread.sleep(200); // Wait for async event processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

