# Edge Cases and Issues Found in Order Management System

This document summarizes all edge cases, boundary conditions, and bugs identified in the order management system.

## 🔴 CRITICAL BUGS

### 1. StatusManager Status Never Initialized to CREATED

**Severity:** CRITICAL  
**Impact:** Prevents ALL operations after order creation

**Problem:**
- When `OrderCreatedEvent` is handled, `OrderStatusManager` is instantiated but `status` is never set to `CREATED`
- `status` remains `null` after order creation
- `statusManager.isCreated()` returns `false` because `status == null`, not `status == CREATED`
- This causes ALL subsequent operations to fail:
  - `AddItemToOrderCommand` fails: "Cannot add items to order in status: null"
  - `RemoveItemFromOrderCommand` fails: "Cannot remove items from order in status: null"
  - `ConfirmOrderCommand` fails: "Order must be in CREATED status to confirm"

**Location:**
- `Order.java:166-177` - `on(OrderCreatedEvent)` handler creates `OrderStatusManager` but doesn't set status
- `OrderStatusManager.java:53-55` - `isCreated()` checks `status == OrderStatus.CREATED`, which is false when status is null

**Fix Required:**
Add an event handler in `OrderStatusManager` for `OrderCreatedEvent`:

```java
@EventSourcingHandler
public void on(OrderCreatedEvent event) {
    this.status = OrderStatus.CREATED;
    this.updatedAt = event.getCreatedAt();
}
```

**Test:** `OrderEdgeCasesTest.testStatusManagerNotInitializedToCreated()`

---

## 🟡 HIGH PRIORITY ISSUES

### 2. No Validation on CreateOrderCommand Fields

**Severity:** HIGH  
**Impact:** Allows invalid data into the system

**Problems:**
- `customerId` can be `null` or empty string
- `customerName` can be `null` or whitespace-only
- `customerEmail` can be `null` or invalid format
- `shippingAddress` can be `null` or empty

**Location:** `Order.java:42-51` - `CreateOrderCommand` handler has no validation

**Recommendation:**
Add validation in command handler or use `@Valid` annotation with Bean Validation:

```java
@CommandHandler
public Order(CreateOrderCommand command) {
    if (command.getCustomerId() == null || command.getCustomerId().trim().isEmpty()) {
        throw new IllegalArgumentException("Customer ID is required");
    }
    if (command.getCustomerEmail() != null && !isValidEmail(command.getCustomerEmail())) {
        throw new IllegalArgumentException("Invalid email format");
    }
    // ... more validations
}
```

**Tests:**
- `OrderEdgeCasesTest.testCreateOrderWithNullCustomerId()`
- `OrderEdgeCasesTest.testCreateOrderWithEmptyCustomerId()`
- `OrderEdgeCasesTest.testCreateOrderWithNullCustomerName()`
- `OrderEdgeCasesTest.testCreateOrderWithWhitespaceOnlyCustomerName()`
- `OrderEdgeCasesTest.testCreateOrderWithInvalidEmail()`
- `OrderEdgeCasesTest.testCreateOrderWithNullEmail()`

---

### 3. No Validation on Product Fields

**Severity:** HIGH  
**Impact:** Allows invalid product data

**Problems:**
- `productId` can be `null` (will cause NPE in event handlers)
- `productName` can be `null` or empty

**Location:** `Order.java:67-74` - `AddItemToOrderCommand` handler

**Recommendation:**
Add null checks before creating events:

```java
if (command.getProductId() == null || command.getProductId().trim().isEmpty()) {
    throw new IllegalArgumentException("Product ID is required");
}
if (command.getProductName() == null || command.getProductName().trim().isEmpty()) {
    throw new IllegalArgumentException("Product name is required");
}
```

**Tests:**
- `OrderEdgeCasesTest.testAddItemWithNullProductId()`
- `OrderEdgeCasesTest.testAddItemWithNullProductName()`

---

### 4. Event Handler Throws RuntimeException

**Severity:** HIGH  
**Impact:** Can crash event handler and prevent event processing

**Problem:**
- `OrderEventHandler` throws `RuntimeException` if `OrderSummary` not found
- This crashes the event handler and may prevent other events from being processed
- No dead letter queue or error recovery mechanism

**Location:** `OrderEventHandler.java:36-37`, `57-58`, `68-69`, `79-80`, `90-91`, `102-103`

**Example:**
```java
OrderSummary orderSummary = orderSummaryRepository.findById(event.getOrderId())
    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));
```

**Recommendation:**
- Use dead letter queue for failed events
- Log error and handle gracefully
- Consider idempotency - if order doesn't exist, maybe it was already deleted

**Test:** `OrderEdgeCasesTest.testEventHandlerThrowsRuntimeException()`

---

## 🟢 MEDIUM PRIORITY ISSUES

### 5. No Maximum Limits on Quantity and Price

**Severity:** MEDIUM  
**Impact:** Potential overflow, memory issues, unrealistic scenarios

**Problems:**
- `quantity` can be `Integer.MAX_VALUE` (2,147,483,647)
- `price` can be arbitrarily large `BigDecimal`
- No business rules to prevent unrealistic values

**Location:** `Order.java:59-65` - Only checks for <= 0, no upper bound

**Recommendation:**
Add reasonable maximum limits:

```java
if (command.getQuantity() > MAX_QUANTITY) {
    throw new IllegalArgumentException("Quantity exceeds maximum allowed: " + MAX_QUANTITY);
}
if (command.getPrice().compareTo(MAX_PRICE) > 0) {
    throw new IllegalArgumentException("Price exceeds maximum allowed");
}
```

**Tests:**
- `OrderEdgeCasesTest.testAddItemWithMaxIntegerQuantity()`
- `OrderEdgeCasesTest.testAddItemWithVeryLargePrice()`

---

### 6. No Decimal Precision Limit on Price

**Severity:** MEDIUM  
**Impact:** May cause precision issues in calculations

**Problem:**
- `price` can have unlimited decimal places
- Business may want to limit to 2 decimal places for currency

**Location:** `Order.java:63-65` - No precision validation

**Recommendation:**
Validate decimal places:

```java
if (command.getPrice().scale() > 2) {
    throw new IllegalArgumentException("Price can have at most 2 decimal places");
}
```

**Test:** `OrderEdgeCasesTest.testAddItemWithHighPrecisionPrice()`

---

### 7. No Maximum String Length Validation

**Severity:** MEDIUM  
**Impact:** Database overflow, memory issues, UI problems

**Problems:**
- `customerName`, `customerEmail`, `shippingAddress` can be extremely long
- Could exceed database column limits
- Could cause memory issues
- Could break UI display

**Location:** All command handlers - no length validation

**Recommendation:**
Add max length validation:

```java
if (command.getCustomerName() != null && command.getCustomerName().length() > MAX_NAME_LENGTH) {
    throw new IllegalArgumentException("Customer name exceeds maximum length");
}
```

**Test:** `OrderEdgeCasesTest.testCreateOrderWithVeryLongCustomerName()`

---

### 8. DELIVERED Status Never Used

**Severity:** MEDIUM  
**Impact:** Dead code or missing functionality

**Problem:**
- `OrderStatus.DELIVERED` exists in enum but:
  - No event handler sets status to `DELIVERED`
  - No command handler transitions to `DELIVERED`
  - No validation checks for `DELIVERED` status

**Location:** `OrderStatusManager.java:77-79` - Enum defined but unused

**Recommendation:**
- Either implement delivery tracking functionality
- Or remove `DELIVERED` from enum if not needed

**Test:** `OrderEdgeCasesTest.testDeliveredStatusNeverSet()`

---

### 9. Duplicate Product Handling Behavior

**Severity:** MEDIUM  
**Impact:** May not match business expectations

**Problem:**
- Adding the same product multiple times **replaces** the existing item instead of accumulating
- This behavior is in `OrderItems.on(ItemAddedToOrderEvent)` which removes existing item first
- May or may not be desired - should be documented

**Location:** `OrderItems.java:22-23`

**Recommendation:**
- Document this behavior clearly
- Consider adding a flag to control replace vs. accumulate behavior
- Or add a separate command like `UpdateItemQuantityCommand`

**Test:** `OrderEdgeCasesTest.testAddSameProductMultipleTimes()`

---

## 🟦 LOW PRIORITY / DOCUMENTATION ISSUES

### 10. No Email Format Validation

**Severity:** LOW  
**Impact:** Invalid email addresses stored in system

**Problem:**
- Email format is not validated
- Invalid emails like "not-an-email" are accepted

**Recommendation:**
Add email format validation using regex or Apache Commons Validator

**Test:** `OrderEdgeCasesTest.testCreateOrderWithInvalidEmail()`

---

### 11. Special Characters in Strings

**Severity:** LOW  
**Impact:** Potential security concerns (though Axon/Spring should handle)

**Problem:**
- No sanitization of special characters
- SQL injection risk (though should be handled by framework)

**Recommendation:**
- Document that framework handles this
- Consider input sanitization for XSS prevention in UI

**Test:** `OrderEdgeCasesTest.testCreateOrderWithSpecialCharacters()`

---

### 12. No Null Checks on statusManager.getStatus() in Error Messages

**Severity:** LOW  
**Impact:** Potential NPE in error messages

**Problem:**
- Error messages call `statusManager.getStatus()` which can return `null`
- Then `.toString()` is called on null in string concatenation
- Actually safe in Java (null.toString() becomes "null"), but could be clearer

**Location:** `Order.java:56`, `80` - Error messages use `statusManager.getStatus()`

**Recommendation:**
Use null-safe string conversion:

```java
"Cannot add items to order in status: " + (statusManager.getStatus() != null ? statusManager.getStatus() : "null")
```

---

## ✅ GOOD VALIDATIONS (Already Implemented)

These validations are correctly implemented:

1. ✅ Quantity must be positive (`> 0`)
2. ✅ Price must be positive (`> 0`)
3. ✅ Cannot confirm order without items
4. ✅ State transition validations (must be in correct status)
5. ✅ Cannot cancel shipped order
6. ✅ Tracking number required and not empty/whitespace
7. ✅ Cancellation reason required and not empty/whitespace
8. ✅ Item must exist before removal

---

## Summary Statistics

- **Critical Bugs:** 1
- **High Priority Issues:** 3
- **Medium Priority Issues:** 6
- **Low Priority Issues:** 3
- **Good Validations:** 8

**Total Edge Cases Documented:** 21

---

## Recommended Fix Priority

1. **IMMEDIATE:** Fix StatusManager initialization bug (#1)
2. **HIGH:** Add validation on CreateOrderCommand (#2)
3. **HIGH:** Add validation on product fields (#3)
4. **HIGH:** Improve event handler error handling (#4)
5. **MEDIUM:** Add maximum limits (#5, #6, #7)
6. **MEDIUM:** Address DELIVERED status (#8)
7. **LOW:** Add email validation and other improvements (#10, #11, #12)

---

## Test Coverage

All edge cases are documented in:
- `src/test/java/com/example/ordermanagement/OrderEdgeCasesTest.java`

Run tests with:
```bash
mvn test -Dtest=OrderEdgeCasesTest
```

Note: Many tests will fail due to the critical StatusManager bug. Fix that first, then re-run tests.

