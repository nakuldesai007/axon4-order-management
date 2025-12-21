# Excel-Based Test Cases for Multi-Day Order Lifecycle

This document explains how to create and use Excel-based test cases for testing the order management system across multiple days, verifying that event sourcing correctly maintains order state.

## Overview

The Excel test case framework allows you to:
- Define test scenarios in Excel (or CSV) files
- Test multi-day order lifecycles (Day-1, Day-2, Day-3)
- Verify event sourcing state reconstruction
- Test frontend-backend integration through REST API
- Maintain test cases in a business-friendly format

## Quick Start

### 1. Test Cases File Location

Place your test case files in:
```
src/test/resources/test-cases/
```

### 2. File Formats Supported

- **Excel (.xlsx)** - Recommended for complex scenarios
- **CSV (.csv)** - Simpler, no dependencies (already provided)

### 3. Running Tests

```bash
# Run all Excel-based tests
mvn test -Dtest=MultiDayOrderLifecycleExcelTest

# Run specific day tests
mvn test -Dtest=MultiDayOrderLifecycleExcelTest#testDay1_*
mvn test -Dtest=MultiDayOrderLifecycleExcelTest#testDay2_*
mvn test -Dtest=MultiDayOrderLifecycleExcelTest#testDay3_*

# Run full lifecycle test
mvn test -Dtest=MultiDayOrderLifecycleExcelTest#testFullLifecycle_Day1ToDay3
```

## Excel File Format

### Required Columns

| Column | Description | Example | Required |
|--------|-------------|---------|----------|
| **Test Case ID** | Unique identifier | TC-001 | Yes |
| **Test Case Name** | Descriptive name | Complete Order Lifecycle | Yes |
| **Description** | Step description | Create order with customer info | Yes |
| **Day** | Day identifier | Day-1, Day-2, Day-3 | Yes |
| **Step Number** | Sequential step number | 1, 2, 3 | Yes |
| **Action** | Action to perform | CREATE_ORDER | Yes |
| **Parameters** | JSON or key=value | customerId=CUST-001 | No |
| **Expected Status** | Expected order status | CREATED | No |
| **Expected Result** | Verification criteria | status=CREATED,itemCount=2 | No |
| **Enabled** | Enable/disable test | Y, N | Yes |

### Column Details

#### Test Case ID
- Unique identifier for grouping related steps
- Multiple steps can share the same Test Case ID
- Example: `TC-001`, `TC-002`

#### Day
- Identifies which day the step belongs to
- Format: `Day-1`, `Day-2`, `Day-3`, etc.
- Steps are executed in day order, then by step number

#### Step Number
- Sequential number within each day
- Steps are executed in ascending order
- Example: 1, 2, 3, 4

#### Action
Supported actions:
- `CREATE_ORDER` - Create a new order
- `ADD_ITEM` - Add item to order
- `REMOVE_ITEM` - Remove item from order
- `CONFIRM_ORDER` - Confirm the order
- `PROCESS_ORDER` - Process the order
- `SHIP_ORDER` - Ship the order
- `CANCEL_ORDER` - Cancel the order
- `VERIFY_ORDER` - Verify order state

#### Parameters
Format: `key1=value1,key2=value2,key3=value3`

Common parameters:
- **CREATE_ORDER:**
  - `customerId` - Customer ID
  - `customerName` - Customer name
  - `customerEmail` - Customer email
  - `shippingAddress` - Shipping address
  - `contextKey` - Custom context key (default: "orderId")

- **ADD_ITEM:**
  - `orderId` - Order ID (or use context key)
  - `productId` - Product ID
  - `productName` - Product name
  - `quantity` - Quantity
  - `price` - Price

- **SHIP_ORDER:**
  - `orderId` - Order ID
  - `trackingNumber` - Tracking number

- **CANCEL_ORDER:**
  - `orderId` - Order ID
  - `reason` - Cancellation reason

#### Expected Status
Expected order status after the step:
- `CREATED`
- `CONFIRMED`
- `PROCESSED`
- `SHIPPED`
- `CANCELLED`

#### Expected Result
Additional verification criteria:
Format: `key1=value1,key2=value2`

Supported keys:
- `status` - Order status
- `itemCount` - Number of items
- `totalAmount` - Total order amount
- `customerId` - Customer ID

Example: `status=CREATED,itemCount=2,totalAmount=1249.98`

#### Enabled
- `Y`, `YES`, `TRUE`, `1` - Test case is enabled
- `N`, `NO`, `FALSE`, `0` - Test case is disabled

## Example Test Scenarios

### Scenario 1: Complete Order Lifecycle (Day-1 to Day-3)

| Test Case ID | Day | Step | Action | Parameters | Expected Status |
|--------------|-----|------|--------|-------------|-----------------|
| TC-001 | Day-1 | 1 | CREATE_ORDER | customerId=CUST-001,customerName=John Doe | CREATED |
| TC-001 | Day-1 | 2 | ADD_ITEM | productId=PROD-001,quantity=1,price=999.99 | CREATED |
| TC-001 | Day-1 | 3 | ADD_ITEM | productId=PROD-002,quantity=1,price=249.99 | CREATED |
| TC-001 | Day-1 | 4 | VERIFY_ORDER | | CREATED |
| TC-001 | Day-2 | 1 | CONFIRM_ORDER | | CONFIRMED |
| TC-001 | Day-2 | 2 | PROCESS_ORDER | | PROCESSED |
| TC-001 | Day-3 | 1 | SHIP_ORDER | trackingNumber=TRK123456789 | SHIPPED |

### Scenario 2: Multi-Order Processing

| Test Case ID | Day | Step | Action | Parameters |
|--------------|-----|------|--------|------------|
| TC-002 | Day-1 | 1 | CREATE_ORDER | contextKey=order1 |
| TC-002 | Day-1 | 2 | CREATE_ORDER | contextKey=order2 |
| TC-002 | Day-1 | 3 | ADD_ITEM | orderId=order1 |
| TC-002 | Day-1 | 4 | ADD_ITEM | orderId=order2 |
| TC-002 | Day-2 | 1 | CONFIRM_ORDER | orderId=order1 |
| TC-002 | Day-2 | 2 | CONFIRM_ORDER | orderId=order2 |

### Scenario 3: Order Cancellation

| Test Case ID | Day | Step | Action | Parameters |
|--------------|-----|------|--------|------------|
| TC-003 | Day-1 | 1 | CREATE_ORDER | |
| TC-003 | Day-1 | 2 | ADD_ITEM | |
| TC-003 | Day-2 | 1 | CONFIRM_ORDER | |
| TC-003 | Day-2 | 2 | CANCEL_ORDER | reason=Customer requested |

## Context Variables

Test steps can reference data from previous steps using context variables:

- **Automatic:** `orderId` is automatically stored after `CREATE_ORDER`
- **Custom:** Use `contextKey` parameter to store with a custom key
  - Example: `contextKey=order1` stores the order ID as "order1"
  - Later steps can reference it: `orderId=order1`

## Event Sourcing Verification

The framework automatically verifies that:
1. **State Persistence:** Order state is correctly persisted after each step
2. **State Reconstruction:** Order state is correctly reconstructed from events
3. **Multi-Day Continuity:** Order state is maintained across days
4. **Read Model Consistency:** Query model matches aggregate state

## Creating Excel Files

### Option 1: Use CSV (Simplest)

The CSV file `multi-day-order-lifecycle.csv` is already provided. You can edit it directly:
```bash
# Edit the CSV file
nano src/test/resources/test-cases/multi-day-order-lifecycle.csv
```

### Option 2: Create Excel File Manually

1. Open Excel or LibreOffice Calc
2. Create columns as specified above
3. Add your test cases
4. Save as `multi-day-order-lifecycle.xlsx` in `src/test/resources/test-cases/`

### Option 3: Use Python Script

```bash
cd src/test/resources/test-cases
./create-excel-template.sh
```

(Requires Python with openpyxl: `pip3 install openpyxl`)

## Test Execution Flow

1. **Read Test Cases:** Load from Excel/CSV file
2. **Group by Test Case:** Group steps by Test Case ID
3. **Group by Day:** Within each test case, group by Day
4. **Execute Steps:** Execute steps in order (Day-1 → Day-2 → Day-3)
5. **Verify State:** After each day, verify order state
6. **Event Sourcing Check:** Verify state reconstruction from events

## Example Output

```
=== Executing Test Case: TC-001 ===

--- Day-1 ---
  Step 1: CREATE_ORDER
    ✓ Order created: 550e8400-e29b-41d4-a716-446655440000
  Step 2: ADD_ITEM
    ✓ Item added to order
  Step 3: ADD_ITEM
    ✓ Item added to order
  Step 4: VERIFY_ORDER
    ✓ Order verification passed

  State after Day-1:
    Order 550e8400... - Status: CREATED, Items: 2, Total: 1249.98

--- Day-2 ---
  Step 1: CONFIRM_ORDER
    ✓ Order confirmed
  Step 2: PROCESS_ORDER
    ✓ Order processed
  Step 3: VERIFY_ORDER
    ✓ Order verification passed

  State after Day-2:
    Order 550e8400... - Status: PROCESSED, Items: 2, Total: 1249.98

--- Day-3 ---
  Step 1: SHIP_ORDER
    ✓ Order shipped with tracking: TRK123456789
  Step 2: VERIFY_ORDER
    ✓ Order verification passed

  State after Day-3:
    Order 550e8400... - Status: SHIPPED, Items: 2, Total: 1249.98
```

## Best Practices

1. **Use Descriptive Test Case Names:** Make it clear what the scenario tests
2. **Group Related Steps:** Use same Test Case ID for related steps
3. **Verify After Each Day:** Add VERIFY_ORDER steps to check state
4. **Use Context Keys:** For multi-order scenarios, use custom context keys
5. **Enable/Disable Tests:** Use Enabled column to temporarily disable tests
6. **Document Parameters:** Add comments in Description column

## Troubleshooting

### Test Cases Not Found
- Ensure file is in `src/test/resources/test-cases/`
- Check file name matches exactly
- Verify file is included in build (check `target/test-classes/`)

### Excel File Not Readable
- Fallback to CSV automatically
- Ensure Excel file is `.xlsx` format (not `.xls`)
- Check column headers match exactly (case-insensitive)

### Steps Fail
- Check that previous steps completed successfully
- Verify orderId is available in context
- Check expected status matches current order state
- Review error messages in test output

## Advanced Usage

### Custom Verification

Add custom verification logic in `executeVerifyOrder()` method:
```java
private void executeVerifyOrder(ExcelTestCaseReader.TestCase step) {
    // Custom verification logic here
}
```

### Multiple Test Files

Create multiple Excel files for different test suites:
- `smoke-tests.xlsx`
- `regression-tests.xlsx`
- `performance-tests.xlsx`

Then create separate test classes for each.

## Integration with CI/CD

Excel test cases can be easily integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run Excel-based tests
  run: mvn test -Dtest=MultiDayOrderLifecycleExcelTest
```

## Benefits

✅ **Business-Friendly:** Test cases in Excel format
✅ **Maintainable:** Easy to update without code changes
✅ **Traceable:** Test cases can be linked to requirements
✅ **Reusable:** Same test cases for manual and automated testing
✅ **Event Sourcing Verified:** Ensures state reconstruction works correctly
✅ **Multi-Day Scenarios:** Tests order lifecycle across time

