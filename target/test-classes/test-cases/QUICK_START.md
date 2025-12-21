# Quick Start: Excel Test Cases

## File Location
```
src/test/resources/test-cases/multi-day-order-lifecycle.csv
```

## CSV Format (Simplest)

The CSV file is ready to use! Just edit it with any text editor or spreadsheet.

### Sample Row:
```csv
TC-001,Complete Order Lifecycle,Create new order,Day-1,1,CREATE_ORDER,"customerId=CUST-001,customerName=John Doe",CREATED,,Y
```

### Column Order:
1. Test Case ID
2. Test Case Name  
3. Description
4. Day
5. Step Number
6. Action
7. Parameters (quoted if contains commas)
8. Expected Status
9. Expected Result
10. Enabled

## Quick Test

```bash
# Run all Excel/CSV tests
mvn test -Dtest=MultiDayOrderLifecycleExcelTest

# Run specific day
mvn test -Dtest=MultiDayOrderLifecycleExcelTest#testDay1_*
```

## Common Actions

- `CREATE_ORDER` - Create order
- `ADD_ITEM` - Add item (needs orderId in params)
- `CONFIRM_ORDER` - Confirm order
- `PROCESS_ORDER` - Process order  
- `SHIP_ORDER` - Ship order (needs trackingNumber)
- `VERIFY_ORDER` - Verify state

## Parameters Format

```
key1=value1,key2=value2,key3=value3
```

Example:
```
customerId=CUST-001,productId=PROD-001,quantity=2,price=99.99
```

## Context Variables

After `CREATE_ORDER`, the orderId is automatically stored. Use it in later steps:
- Default: `orderId` (automatic)
- Custom: `contextKey=myOrder` then use `orderId=myOrder`

## See Full Documentation

See `EXCEL_TEST_CASES.md` in project root for complete documentation.

