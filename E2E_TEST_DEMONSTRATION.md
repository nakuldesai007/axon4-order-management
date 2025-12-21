# E2E Test Demonstration - How It Works

## Overview

The `OrderE2ETest` class uses **Selenium WebDriver** to control a real Chrome browser and test the actual UI interacting with the backend.

## How It Works

### 1. Test Setup

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderE2ETest {
    
    @LocalServerPort
    private int backendPort;  // Spring Boot assigns random port
    
    private WebDriver driver;  // Chrome browser instance
    private String baseUrl;    // http://localhost:{port}
    
    @BeforeEach
    void setUp() {
        // 1. Clean database
        orderSummaryRepository.deleteAll();
        
        // 2. Start Chrome browser (headless mode)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // No visible window
        driver = new ChromeDriver(options);
        
        // 3. Set base URL
        baseUrl = "http://localhost:" + backendPort;
    }
}
```

### 2. Test Execution Flow

Here's what happens when `testCreateOrder_ThroughUI()` runs:

#### Step 1: Navigate to Create Order Page
```java
driver.get(baseUrl + "/create");
```
**What happens:**
- Browser opens `http://localhost:3117/create`
- Spring Boot serves the React frontend (from `static/` folder)
- React app loads and renders the order form

#### Step 2: Fill Form Fields
```java
WebElement customerIdField = driver.findElement(By.id("customerId"));
customerIdField.sendKeys("CUST-E2E-001");
```
**What happens:**
- Browser finds the `<input id="customerId">` element
- Types "CUST-E2E-001" into the field (simulates user typing)
- Same as if a real user was typing in the browser

#### Step 3: Submit Form
```java
WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
submitButton.click();
```
**What happens:**
- Browser finds the submit button
- Clicks it (simulates user clicking)
- React form handler calls `orderService.createOrder()`
- Frontend makes HTTP POST to `/api/orders`
- Backend processes command and publishes event
- React navigates to `/orders/{orderId}`

#### Step 4: Verify Results
```java
wait.until(ExpectedConditions.urlContains("/orders/"));
WebElement statusBadge = driver.findElement(By.className("status-badge"));
assertTrue(statusBadge.getText().contains("CREATED"));
```
**What happens:**
- Browser waits for URL to change to `/orders/{orderId}`
- Finds the status badge element in the rendered HTML
- Verifies it displays "CREATED"
- This confirms both UI rendering AND backend state

## Complete Test Flow Example

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Test Starts                                              │
│    - Spring Boot starts on random port (e.g., 3117)        │
│    - Chrome browser opens (headless)                        │
│    - Database is cleaned                                    │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Navigate to Create Page                                  │
│    driver.get("http://localhost:3117/create")              │
│    → Browser loads React app                                │
│    → React renders OrderForm component                      │
│    → HTML is rendered with form fields                      │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. Fill Form (User Simulation)                              │
│    driver.findElement(By.id("customerId"))                  │
│      .sendKeys("CUST-E2E-001")                              │
│    → Browser types into input field                         │
│    → React state updates                                    │
│    → Same as user typing in browser                         │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. Submit Form                                              │
│    driver.findElement(By.cssSelector("button[type='submit']"))│
│      .click()                                               │
│    → Browser clicks button                                  │
│    → React onSubmit handler fires                           │
│    → Frontend calls: POST /api/orders                      │
│    → Backend receives CreateOrderRequest                    │
│    → CommandGateway sends CreateOrderCommand                │
│    → Aggregate publishes OrderCreatedEvent                  │
│    → Event handler updates read model                       │
│    → React navigates to /orders/{orderId}                   │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. Verify UI and Backend State                              │
│    wait.until(ExpectedConditions.urlContains("/orders/"))    │
│    → Browser waits for navigation                           │
│    → React loads OrderDetails component                     │
│    → Frontend calls: GET /api/orders/{orderId}             │
│    → Backend queries read model                             │
│    → React renders order details                            │
│    → Browser finds status badge element                     │
│    → Verifies text contains "CREATED"                       │
│    → Test passes! ✅                                        │
└─────────────────────────────────────────────────────────────┘
```

## What Gets Tested

### ✅ Frontend (UI)
- Form rendering
- User input handling
- Button clicks
- Navigation
- Data display
- React component behavior

### ✅ Backend (API)
- REST endpoints
- Command processing
- Event publishing
- Read model updates
- Database persistence

### ✅ Integration
- Frontend → Backend communication
- API responses
- State synchronization
- Event sourcing consistency

## Test Scenarios Included

1. **testCreateOrder_ThroughUI**
   - User fills form → Submits → Verifies order created

2. **testViewOrderList_DisplaysOrders**
   - User views order list → Verifies orders displayed

3. **testCreateOrderAndAddItems_ThroughUI**
   - User creates order → Adds items → Verifies items appear

4. **testConfirmOrder_ThroughUI**
   - User confirms order → Verifies status changes

5. **testFilterOrdersByStatus_ThroughUI**
   - User clicks filter buttons → Verifies filtering works

6. **testNavigation_ThroughUI**
   - User navigates between pages → Verifies routing

7. **testFormValidation_ThroughUI**
   - User submits invalid form → Verifies validation

## Running the Tests

### Prerequisites
1. **Chrome Browser** installed
2. **Frontend built** (`npm run build`)
3. **Backend dependencies** (handled by Maven)

### Command
```bash
# Build frontend first
cd frontend && npm run build && cd ..

# Run E2E tests
mvn test -Dtest=OrderE2ETest
```

### What You'll See

When tests run successfully:
```
✅ testCreateOrder_ThroughUI - PASSED
   - Browser opened Chrome
   - Navigated to /create
   - Filled form fields
   - Submitted form
   - Verified order created
   - Verified UI displays order details

✅ testViewOrderList_DisplaysOrders - PASSED
   - Navigated to home page
   - Verified order list renders
   - Verified orders are displayed

... (more tests)
```

## Key Advantages

1. **Real Browser Testing** - Tests actual Chrome browser, not simulation
2. **Full Stack** - Tests UI + Backend + Database together
3. **User Perspective** - Tests from user's point of view
4. **Confidence** - Highest confidence that system works end-to-end
5. **Catch Integration Issues** - Finds problems API tests might miss

## Limitations

1. **Slower** - Browser tests are slower than API tests
2. **More Flaky** - Can be affected by timing, network, etc.
3. **Requires Browser** - Needs Chrome/Chromium installed
4. **More Complex** - Harder to debug when they fail

## Best Practice: Use Both

- **API Tests** (Fast, Reliable) - For most integration testing
- **E2E Tests** (Slow, High Confidence) - For critical user journeys

Your current setup:
- ✅ `FullStackIntegrationTest` - API-level integration tests (fast)
- ✅ `MultiDayOrderLifecycleExcelTest` - Data-driven API tests (flexible)
- ✅ `OrderE2ETest` - Browser-based E2E tests (high confidence)

## Example: What the Browser Sees

When the test runs, the browser (in headless mode) sees:

```html
<!-- Page: /create -->
<form>
  <input id="customerId" type="text" />
  <input id="customerName" type="text" />
  <input id="customerEmail" type="email" />
  <textarea id="shippingAddress"></textarea>
  <button type="submit">Create Order</button>
</form>

<!-- After submission: /orders/{orderId} -->
<div class="order-details">
  <h2>Order Details</h2>
  <p>ID: {orderId}</p>
  <span class="status-badge">CREATED</span>
  <p>Customer: E2E Test Customer</p>
  ...
</div>
```

The test verifies that:
1. Elements exist in the DOM
2. Text content is correct
3. Navigation works
4. Backend data is displayed correctly

This is **real end-to-end testing** - testing the actual UI and backend together! 🎯

