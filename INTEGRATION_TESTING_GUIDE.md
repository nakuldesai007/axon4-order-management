# Integration Testing Guide: UI and Backend

This guide covers best practices for writing integration tests that verify both the UI (frontend) and backend work together correctly.

## Table of Contents

1. [Testing Approaches](#testing-approaches)
2. [Backend API Integration Tests](#backend-api-integration-tests)
3. [Full-Stack Integration Tests](#full-stack-integration-tests)
4. [Best Practices](#best-practices)
5. [Example Test Scenarios](#example-test-scenarios)

---

## Testing Approaches

### 1. **API-Level Integration Tests** (Recommended for Most Cases)
- Test the REST API endpoints directly using `TestRestTemplate`
- Simulates what the frontend does (HTTP requests)
- Fast, reliable, and easy to maintain
- **Best for:** Most integration testing scenarios

### 2. **Full-Stack Integration Tests** (For Critical User Flows)
- Test the actual UI using Selenium/Playwright
- Tests real user interactions
- Slower but provides highest confidence
- **Best for:** Critical user journeys, E2E smoke tests

### 3. **Hybrid Approach** (Recommended)
- Use API tests for most scenarios (fast, reliable)
- Use UI tests for critical user flows (high confidence)
- Combine both for comprehensive coverage

---

## Backend API Integration Tests

### Current Approach (Recommended)

Your current `MultiDayOrderLifecycleExcelTest` is an excellent example of API-level integration testing:

**Advantages:**
- ✅ Fast execution
- ✅ Reliable (no browser dependencies)
- ✅ Easy to maintain
- ✅ Tests the same API the frontend uses
- ✅ Can test async event processing
- ✅ Verifies read model consistency

**Example Pattern:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private OrderSummaryRepository repository;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll(); // Clean state
    }
    
    @Test
    void testCreateOrder_ThroughAPI_ShouldWork() {
        // Simulate frontend API call
        CreateOrderRequest request = new CreateOrderRequest(...);
        ResponseEntity<String> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/orders", 
            request, 
            String.class
        );
        
        // Verify response
        assertEquals(200, response.getStatusCodeValue());
        String orderId = response.getBody();
        
        // Wait for async event processing
        await().atMost(2, SECONDS).until(() -> 
            repository.findById(orderId).isPresent()
        );
        
        // Verify read model
        OrderSummary order = repository.findById(orderId).orElseThrow();
        assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
    }
}
```

---

## Full-Stack Integration Tests

### Option 1: API Simulation (Current - Recommended)

**What it does:**
- Makes HTTP requests to backend API (same as frontend)
- Verifies responses and read model state
- Tests event sourcing and CQRS consistency

**When to use:**
- ✅ Most integration test scenarios
- ✅ Testing business logic and workflows
- ✅ Verifying event sourcing state reconstruction
- ✅ Testing async event processing

**Example:**
```java
// Simulates: User fills form → Frontend calls API → Backend processes → UI updates
@Test
void testCompleteOrderLifecycle_ThroughAPI() {
    // 1. Create order (simulates UI form submission)
    CreateOrderRequest createRequest = new CreateOrderRequest(...);
    String orderId = restTemplate.postForObject(baseUrl, createRequest, String.class);
    
    // 2. Add items (simulates UI item management)
    AddItemRequest itemRequest = new AddItemRequest(...);
    restTemplate.postForObject(baseUrl + "/" + orderId + "/items", itemRequest, Void.class);
    
    // 3. Verify state (simulates UI refresh)
    OrderSummary order = repository.findById(orderId).orElseThrow();
    assertEquals(1, order.getItems().size());
    
    // 4. Confirm order (simulates UI button click)
    restTemplate.postForObject(baseUrl + "/" + orderId + "/confirm", null, Void.class);
    
    // 5. Verify final state
    await().atMost(2, SECONDS).until(() -> {
        OrderSummary o = repository.findById(orderId).orElseThrow();
        return o.getStatus() == OrderSummary.OrderStatus.CONFIRMED;
    });
}
```

### Option 2: Browser-Based UI Tests (For Critical Flows)

**What it does:**
- Uses Selenium/Playwright to control a real browser
- Tests actual UI interactions
- Verifies visual elements and user experience

**When to use:**
- ✅ Critical user journeys (happy path)
- ✅ UI-specific features (forms, validation, navigation)
- ✅ Cross-browser compatibility
- ⚠️ Use sparingly (slow, flaky)

**Setup Example (Playwright):**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderUITest {
    
    @LocalServerPort
    private int port;
    
    private Playwright playwright;
    private Browser browser;
    private Page page;
    
    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        page = browser.newPage();
    }
    
    @AfterEach
    void tearDown() {
        browser.close();
        playwright.close();
    }
    
    @Test
    void testCreateOrder_ThroughUI() {
        // Navigate to UI
        page.navigate("http://localhost:" + port);
        
        // Fill form (simulates user input)
        page.fill("input[name='customerName']", "John Doe");
        page.fill("input[name='customerEmail']", "john@example.com");
        page.fill("input[name='shippingAddress']", "123 Main St");
        
        // Submit form (simulates button click)
        page.click("button[type='submit']");
        
        // Wait for navigation and verify
        page.waitForURL("**/orders/*");
        String url = page.url();
        assertTrue(url.contains("/orders/"));
    }
}
```

---

## Best Practices

### 1. **Test Structure: Arrange-Act-Assert (AAA)**

```java
@Test
void testOrderCreation() {
    // ARRANGE: Set up test data and state
    CreateOrderRequest request = new CreateOrderRequest(...);
    repository.deleteAll(); // Clean state
    
    // ACT: Execute the operation
    ResponseEntity<String> response = restTemplate.postForEntity(
        baseUrl, request, String.class
    );
    
    // ASSERT: Verify the results
    assertEquals(200, response.getStatusCodeValue());
    assertNotNull(response.getBody());
}
```

### 2. **Clean State Between Tests**

```java
@BeforeEach
void setUp() {
    // Always start with clean state
    orderSummaryRepository.deleteAll();
    // Reset any test context
    testContext.clear();
}
```

### 3. **Handle Async Operations**

```java
// Wait for event processing
await().atMost(2, SECONDS).until(() -> 
    repository.findById(orderId).isPresent()
);

// Or use explicit waits
Thread.sleep(200); // Simple but less reliable
```

### 4. **Test Real User Flows**

```java
// Good: Tests complete user journey
@Test
void testCompleteOrderFlow() {
    // 1. User creates order
    String orderId = createOrder();
    
    // 2. User adds items
    addItem(orderId);
    
    // 3. User confirms order
    confirmOrder(orderId);
    
    // 4. User ships order
    shipOrder(orderId);
    
    // 5. Verify final state
    verifyOrderShipped(orderId);
}

// Avoid: Testing implementation details
@Test
void testCommandGateway() { // Too low-level
    // ...
}
```

### 5. **Use Test Data Builders**

```java
class OrderRequestBuilder {
    private String customerId = "CUST-001";
    private String customerName = "Test Customer";
    // ...
    
    OrderRequestBuilder withCustomerId(String id) {
        this.customerId = id;
        return this;
    }
    
    CreateOrderRequest build() {
        return new CreateOrderRequest(customerId, customerName, ...);
    }
}

// Usage
CreateOrderRequest request = new OrderRequestBuilder()
    .withCustomerId("CUST-123")
    .withCustomerName("John Doe")
    .build();
```

### 6. **Verify Both Command and Query Sides**

```java
@Test
void testOrderCreation_VerifiesBothSides() {
    // Command side: Send command
    String orderId = restTemplate.postForObject(baseUrl, request, String.class);
    
    // Wait for event processing
    await().atMost(2, SECONDS).until(() -> 
        repository.findById(orderId).isPresent()
    );
    
    // Query side: Verify read model
    OrderSummary order = repository.findById(orderId).orElseThrow();
    assertEquals(OrderSummary.OrderStatus.CREATED, order.getStatus());
    assertEquals("John Doe", order.getCustomerName());
}
```

### 7. **Test Error Scenarios**

```java
@Test
void testCreateOrder_WithInvalidData_ShouldReturnError() {
    CreateOrderRequest invalidRequest = new CreateOrderRequest();
    // Missing required fields
    
    ResponseEntity<String> response = restTemplate.postForEntity(
        baseUrl, invalidRequest, String.class
    );
    
    assertEquals(400, response.getStatusCodeValue());
}
```

### 8. **Use External Test Data (Excel/CSV)**

```java
// Read test cases from CSV/Excel
List<TestCase> testCases = CSVTestCaseReader.readTestCases("test-cases/scenarios.csv");

for (TestCase testCase : testCases) {
    executeTestCase(testCase); // Data-driven testing
}
```

---

## Example Test Scenarios

### Scenario 1: Complete Order Lifecycle

```java
@Test
@DisplayName("Complete order lifecycle: Create → Add Items → Confirm → Process → Ship")
void testCompleteOrderLifecycle() {
    // 1. Create order (UI: User fills form and submits)
    String orderId = createOrderViaAPI("CUST-001", "John Doe", ...);
    
    // 2. Add items (UI: User adds products to cart)
    addItemViaAPI(orderId, "PROD-001", "iPhone", 1, 999.99);
    addItemViaAPI(orderId, "PROD-002", "AirPods", 1, 249.99);
    
    // 3. Verify items added (UI: Cart shows items)
    OrderSummary order = getOrderViaAPI(orderId);
    assertEquals(2, order.getItems().size());
    assertEquals(new BigDecimal("1249.98"), order.getTotalAmount());
    
    // 4. Confirm order (UI: User clicks "Confirm Order")
    confirmOrderViaAPI(orderId);
    await().until(() -> getOrderViaAPI(orderId).getStatus() == CONFIRMED);
    
    // 5. Process order (UI: Admin processes order)
    processOrderViaAPI(orderId);
    await().until(() -> getOrderViaAPI(orderId).getStatus() == PROCESSED);
    
    // 6. Ship order (UI: Admin adds tracking and ships)
    shipOrderViaAPI(orderId, "TRK123456789");
    await().until(() -> getOrderViaAPI(orderId).getStatus() == SHIPPED);
    
    // 7. Final verification
    OrderSummary finalOrder = getOrderViaAPI(orderId);
    assertEquals(SHIPPED, finalOrder.getStatus());
    assertEquals("TRK123456789", finalOrder.getTrackingNumber());
}
```

### Scenario 2: Multi-User Concurrent Operations

```java
@Test
@DisplayName("Multiple users creating orders concurrently")
void testConcurrentOrderCreation() {
    int numberOfUsers = 10;
    List<CompletableFuture<String>> futures = new ArrayList<>();
    
    // Simulate multiple users creating orders simultaneously
    for (int i = 0; i < numberOfUsers; i++) {
        final int userId = i;
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return createOrderViaAPI("CUST-" + userId, "User " + userId, ...);
        });
        futures.add(future);
    }
    
    // Wait for all to complete
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .get(10, TimeUnit.SECONDS);
    
    // Verify all orders created
    assertEquals(numberOfUsers, repository.count());
}
```

### Scenario 3: Error Handling

```java
@Test
@DisplayName("Handle invalid operations gracefully")
void testErrorHandling() {
    String orderId = createOrderViaAPI(...);
    
    // Try to confirm order without items (should fail)
    ResponseEntity<String> response = restTemplate.postForEntity(
        baseUrl + "/" + orderId + "/confirm", null, String.class
    );
    
    assertEquals(409, response.getStatusCodeValue()); // Conflict
    assertTrue(response.getBody().contains("Cannot confirm order without items"));
}
```

---

## Recommended Test Structure

```
src/test/java/com/example/ordermanagement/
├── OrderAggregateTest.java              # Unit tests (AggregateTestFixture)
├── OrderEdgeCasesTest.java              # Edge case unit tests (CSV-driven)
├── OrderCreationIntegrationTest.java    # API integration tests
├── OrderIntegrationTest.java            # Full workflow integration tests
├── excel/
│   ├── MultiDayOrderLifecycleExcelTest.java  # Data-driven integration tests
│   └── CSVTestCaseReader.java
└── e2e/                                 # Optional: Browser-based E2E tests
    └── OrderE2ETest.java
```

---

## Summary

**For your Axon Framework project, the recommended approach is:**

1. ✅ **API-Level Integration Tests** (Primary)
   - Use `TestRestTemplate` to simulate frontend API calls
   - Test complete workflows through REST API
   - Verify event sourcing and CQRS consistency
   - Fast, reliable, maintainable

2. ✅ **Data-Driven Tests** (Current approach)
   - Use Excel/CSV for test case management
   - Business-friendly test data
   - Easy to add/modify scenarios

3. ⚠️ **Browser-Based Tests** (Optional, for critical flows only)
   - Use sparingly for critical user journeys
   - Slower but provides highest confidence
   - Consider Playwright over Selenium

**Your current `MultiDayOrderLifecycleExcelTest` is an excellent example of best practices!**

