# E2E Test Walkthrough - What Happens When Tests Run

This document shows exactly what happens when you run the browser-based E2E tests.

## Test Execution Flow

### Test: `testCreateOrder_ThroughUI`

Here's what happens step-by-step when this test runs:

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: Test Setup                                          │
├─────────────────────────────────────────────────────────────┤
│ • Spring Boot starts on random port (e.g., 3117)            │
│ • Chrome browser opens (headless mode - no visible window)  │
│ • Database is cleaned (orderSummaryRepository.deleteAll()) │
│ • baseUrl = "http://localhost:3117"                         │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 2: Navigate to Create Order Page                        │
├─────────────────────────────────────────────────────────────┤
│ Code: driver.get(baseUrl + "/create")                       │
│                                                              │
│ What Browser Sees:                                           │
│ ┌────────────────────────────────────────┐                 │
│ │  Order Management System                │                 │
│ │  ──────────────────────────            │                 │
│ │                                        │                 │
│ │  Create New Order                      │                 │
│ │  ────────────────                      │                 │
│ │                                        │                 │
│ │  Customer ID *                         │                 │
│ │  [________________]                    │                 │
│ │                                        │                 │
│ │  Customer Name *                       │                 │
│ │  [________________]                    │                 │
│ │                                        │                 │
│ │  Customer Email                        │                 │
│ │  [________________]                    │                 │
│ │                                        │                 │
│ │  Shipping Address                      │                 │
│ │  [________________]                    │                 │
│ │                                        │                 │
│ │  [Create Order]  [Cancel]              │                 │
│ └────────────────────────────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 3: Fill Form Fields (Simulates User Typing)            │
├─────────────────────────────────────────────────────────────┤
│ Code:                                                        │
│   customerIdField.sendKeys("CUST-E2E-001")                  │
│   customerNameField.sendKeys("E2E Test Customer")            │
│   customerEmailField.sendKeys("e2e@test.com")                │
│   shippingAddressField.sendKeys("123 E2E Test Street...")    │
│                                                              │
│ What Browser Sees (After Typing):                           │
│ ┌────────────────────────────────────────┐                 │
│ │  Customer ID *                          │                 │
│ │  [CUST-E2E-001______]  ← Typed by test  │                 │
│ │                                        │                 │
│ │  Customer Name *                       │                 │
│ │  [E2E Test Customer___]  ← Typed       │                 │
│ │                                        │                 │
│ │  Customer Email                        │                 │
│ │  [e2e@test.com_______]  ← Typed        │                 │
│ │                                        │                 │
│ │  Shipping Address                      │                 │
│ │  [123 E2E Test Street...]  ← Typed     │                 │
│ └────────────────────────────────────────┘                 │
│                                                              │
│ React State Updates:                                         │
│   formData = {                                              │
│     customerId: "CUST-E2E-001",                             │
│     customerName: "E2E Test Customer",                     │
│     customerEmail: "e2e@test.com",                         │
│     shippingAddress: "123 E2E Test Street..."              │
│   }                                                          │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 4: Submit Form (Simulates User Clicking Button)        │
├─────────────────────────────────────────────────────────────┤
│ Code:                                                        │
│   submitButton.click()                                       │
│                                                              │
│ What Happens:                                                │
│ 1. Browser clicks the "Create Order" button                 │
│ 2. React onSubmit handler fires                             │
│ 3. Frontend calls: POST /api/orders                        │
│    Request Body: {                                          │
│      customerId: "CUST-E2E-001",                            │
│      customerName: "E2E Test Customer",                     │
│      customerEmail: "e2e@test.com",                         │
│      shippingAddress: "123 E2E Test Street..."              │
│    }                                                         │
│ 4. Backend receives request                                 │
│ 5. OrderController.createOrder() processes it               │
│ 6. CommandGateway sends CreateOrderCommand                  │
│ 7. Order aggregate handles command                          │
│ 8. OrderCreatedEvent is published                           │
│ 9. Event handler updates read model                         │
│ 10. React receives orderId in response                       │
│ 11. React navigates to /orders/{orderId}                    │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 5: Wait for Navigation                                 │
├─────────────────────────────────────────────────────────────┤
│ Code:                                                        │
│   wait.until(ExpectedConditions.urlContains("/orders/"))    │
│                                                              │
│ What Browser Sees:                                           │
│ • URL changes from /create to /orders/{orderId}            │
│ • React Router handles navigation                            │
│ • OrderDetails component loads                               │
│ • Component calls: GET /api/orders/{orderId}               │
│ • Backend queries read model                                │
│ • React renders order details                                │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ STEP 6: Verify Order Details Page                            │
├─────────────────────────────────────────────────────────────┤
│ What Browser Sees:                                           │
│ ┌────────────────────────────────────────┐                 │
│ │  ← Back to Orders                       │                 │
│ │                                        │                 │
│ │  Order Details                         │                 │
│ │  ID: 550e8400-e29b-41d4-a716-...       │                 │
│ │  Status: [CREATED]  ← Verified by test │                 │
│ │                                        │                 │
│ │  Customer Information                  │                 │
│ │  Name: E2E Test Customer  ← Verified   │                 │
│ │  ID: CUST-E2E-001                      │                 │
│ │  Email: e2e@test.com                   │                 │
│ │  Shipping: 123 E2E Test Street...      │                 │
│ │                                        │                 │
│ │  Order Summary                         │
│ │  Items: 0                              │                 │
│ │  Total: $0.00                          │                 │
│ └────────────────────────────────────────┘                 │
│                                                              │
│ Code Verification:                                           │
│   statusBadge = driver.findElement(By.className("status-badge"))│
│   assertTrue(statusBadge.getText().contains("CREATED"))     │
│                                                              │
│ ✅ TEST PASSES!                                              │
│   • UI rendered correctly                                   │
│   • Form submission worked                                  │
│   • Backend processed command                               │
│   • Event was published                                     │
│   • Read model was updated                                  │
│   • UI displays correct data                                │
└─────────────────────────────────────────────────────────────┘
```

## Complete Test Scenario: Create Order and Add Items

```
┌─────────────────────────────────────────────────────────────┐
│ TEST: testCreateOrderAndAddItems_ThroughUI                   │
└─────────────────────────────────────────────────────────────┘

1. CREATE ORDER
   ┌─────────────────────────┐
   │ Browser: /create        │
   │ User types form data    │
   │ Clicks "Create Order"   │
   └─────────────────────────┘
            ↓
   ┌─────────────────────────┐
   │ Backend: Creates order  │
   │ Returns: orderId         │
   └─────────────────────────┘
            ↓
   ┌─────────────────────────┐
   │ Browser: /orders/{id}   │
   │ Shows: Order Details    │
   └─────────────────────────┘

2. ADD ITEM
   ┌─────────────────────────┐
   │ Browser: Clicks         │
   │ "+ Add Item" button     │
   └─────────────────────────┘
            ↓
   ┌─────────────────────────┐
   │ Form appears:           │
   │ • Product ID            │
   │ • Product Name          │
   │ • Quantity              │
   │ • Price                 │
   └─────────────────────────┘
            ↓
   ┌─────────────────────────┐
   │ User fills form:        │
   │ • PROD-E2E-001         │
   │ • E2E Test Product      │
   │ • Quantity: 2           │
   │ • Price: 99.99          │
   └─────────────────────────┘
            ↓
   ┌─────────────────────────┐
   │ Clicks "Add Item"       │
   └─────────────────────────┘
            ↓
   ┌─────────────────────────┐
   │ Frontend: POST          │
   │ /api/orders/{id}/items  │
   └─────────────────────────┘
            ↓
   ┌─────────────────────────┐
   │ Backend: Processes       │
   │ ItemAddedToOrderEvent    │
   └─────────────────────────┘
            ↓
   ┌─────────────────────────┐
   │ UI Updates:             │
   │ • Item appears in list  │
   │ • Total amount updates  │
   │ • Item count: 1         │
   └─────────────────────────┘
            ↓
   ┌─────────────────────────┐
   │ Test Verifies:           │
   │ • Item is displayed     │
   │ • Quantity is correct   │
   │ • Price is correct      │
   └─────────────────────────┘
            ↓
   ✅ TEST PASSES!
```

## What Gets Tested

### ✅ Frontend (React UI)
- **Form Rendering**: HTML form elements are present
- **User Input**: Text fields accept input
- **Button Clicks**: Buttons are clickable and trigger actions
- **Navigation**: React Router navigation works
- **Data Display**: Order details are displayed correctly
- **State Updates**: React state updates reflect backend changes
- **Component Behavior**: React components render and update

### ✅ Backend (Spring Boot API)
- **REST Endpoints**: API endpoints respond correctly
- **Command Processing**: Commands are handled by aggregates
- **Event Publishing**: Events are published to event bus
- **Read Model Updates**: Event handlers update read model
- **Database Persistence**: Data is stored in database

### ✅ Integration
- **Frontend ↔ Backend**: HTTP communication works
- **State Synchronization**: UI reflects backend state
- **Event Sourcing**: State is correctly reconstructed
- **CQRS**: Command and query sides are consistent

## Running the Tests

### When Chrome is Available:

```bash
# 1. Build frontend
cd frontend && npm run build && cd ..

# 2. Run E2E tests
mvn test -Dtest=OrderE2ETest

# Output:
# ✅ testCreateOrder_ThroughUI - PASSED
#    • Browser opened Chrome
#    • Navigated to /create
#    • Filled form fields
#    • Submitted form
#    • Verified order created
#    • Verified UI displays order details
#
# ✅ testViewOrderList_DisplaysOrders - PASSED
# ✅ testCreateOrderAndAddItems_ThroughUI - PASSED
# ...
```

### What You'll See (if browser visible):

1. **Chrome opens** (or runs headless)
2. **Navigates** to your application
3. **Types** into form fields (you see text appearing)
4. **Clicks** buttons (you see button press)
5. **Page changes** (navigation happens)
6. **Data appears** (order details render)

## Test Output Example

```
============================================================
E2E TEST EXECUTION
============================================================

📋 TEST: User creates order through UI

1️⃣  SETUP:
   • Spring Boot started on port 3117
   • Chrome browser opened (headless)
   • Database cleaned

2️⃣  NAVIGATE TO CREATE PAGE:
   • Browser navigated to http://localhost:3117/create
   • React app loaded
   • OrderForm component rendered
   • Form fields are present ✓

3️⃣  FILL FORM:
   • Typed "CUST-E2E-001" into customerId field
   • Typed "E2E Test Customer" into customerName field
   • Typed "e2e@test.com" into customerEmail field
   • Typed "123 E2E Test Street..." into shippingAddress field
   • All fields filled successfully ✓

4️⃣  SUBMIT FORM:
   • Clicked "Create Order" button
   • Frontend: POST /api/orders
   • Backend: Created order with ID: 550e8400-e29b-41d4-a716-446655440000
   • Event: OrderCreatedEvent published
   • Read model: OrderSummary created
   • React: Navigated to /orders/550e8400-e29b-41d4-a716-446655440000
   • Navigation successful ✓

5️⃣  VERIFY RESULTS:
   • URL contains "/orders/" ✓
   • Order details page loaded ✓
   • Status badge found ✓
   • Status badge text: "CREATED" ✓
   • Customer name displayed: "E2E Test Customer" ✓

✅ TEST PASSED!
   • UI rendered correctly
   • Form submission worked
   • Backend processed command
   • Event was published
   • Read model was updated
   • UI displays correct data

============================================================
```

## Key Differences from API Tests

| Aspect | API Tests | E2E Tests |
|--------|-----------|-----------|
| **Browser** | ❌ No browser | ✅ Real Chrome browser |
| **UI Rendering** | ❌ Not tested | ✅ Fully tested |
| **User Interactions** | ❌ Simulated | ✅ Real clicks/typing |
| **React Components** | ❌ Not tested | ✅ Component behavior tested |
| **Speed** | ✅ Fast (~1s) | ❌ Slower (~5-10s) |
| **Reliability** | ✅ Very reliable | ⚠️ Can be flaky |
| **Setup** | ✅ Simple | ❌ Requires Chrome |

## Summary

The E2E tests use a **real Chrome browser** to:
1. ✅ Navigate to your application
2. ✅ Interact with UI elements (click, type)
3. ✅ Verify UI displays correctly
4. ✅ Test complete user workflows
5. ✅ Verify frontend and backend work together

This provides the **highest confidence** that your system works end-to-end from the user's perspective! 🎯

