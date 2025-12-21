# Excel Test Cases

This directory contains Excel files with test case definitions for multi-day order lifecycle scenarios.

## File Structure

- `multi-day-order-lifecycle.xlsx` - Main test case file with Day-1, Day-2, Day-3 scenarios

## Excel File Format

The Excel file should have the following columns:

| Column Name | Description | Example |
|------------|-------------|---------|
| Test Case ID | Unique identifier for the test case | TC-001 |
| Test Case Name | Descriptive name | Complete Order Lifecycle |
| Description | Step description | Create order with customer info |
| Day | Day identifier | Day-1, Day-2, Day-3 |
| Step Number | Sequential step number within day | 1, 2, 3 |
| Action | Action to perform | CREATE_ORDER, ADD_ITEM, CONFIRM_ORDER, etc. |
| Parameters | JSON or key=value parameters | customerId=CUST-001,productId=PROD-001 |
| Expected Status | Expected order status after step | CREATED, CONFIRMED, PROCESSED, SHIPPED |
| Expected Result | Additional verification criteria | status=CONFIRMED,itemCount=2 |
| Enabled | Whether test case is enabled | Y, N, YES, NO, TRUE, FALSE, 1, 0 |

## Supported Actions

- `CREATE_ORDER` - Create a new order
- `ADD_ITEM` - Add item to order
- `REMOVE_ITEM` - Remove item from order
- `CONFIRM_ORDER` - Confirm the order
- `PROCESS_ORDER` - Process the order
- `SHIP_ORDER` - Ship the order
- `CANCEL_ORDER` - Cancel the order
- `VERIFY_ORDER` - Verify order state

## Parameters Format

Parameters can be specified in two formats:

1. **Key=Value pairs (comma-separated):**
   ```
   customerId=CUST-001,productId=PROD-001,quantity=2,price=99.99
   ```

2. **Simple JSON format:**
   ```
   {customerId: CUST-001, productId: PROD-001, quantity: 2, price: 99.99}
   ```

## Context Variables

Test steps can reference previous steps using context variables:
- `orderId` - Automatically stored after CREATE_ORDER
- Custom context keys can be set using `contextKey` parameter

## Example Test Case Flow

### Day-1: Order Creation
1. CREATE_ORDER → Creates order, stores orderId in context
2. ADD_ITEM → Adds item to the created order
3. ADD_ITEM → Adds another item
4. VERIFY_ORDER → Verifies order has 2 items, status CREATED

### Day-2: Order Processing
1. CONFIRM_ORDER → Confirms the order (uses orderId from context)
2. PROCESS_ORDER → Processes the confirmed order
3. VERIFY_ORDER → Verifies status is PROCESSED

### Day-3: Order Shipping
1. SHIP_ORDER → Ships the order with tracking number
2. VERIFY_ORDER → Verifies status is SHIPPED, tracking number exists

## Running Tests

Tests are automatically executed by `MultiDayOrderLifecycleExcelTest`:
- Individual day tests: `testDay1_*`, `testDay2_*`, `testDay3_*`
- Full lifecycle: `testFullLifecycle_Day1ToDay3`
- All test cases: `testExecuteAllTestCasesFromExcel`

