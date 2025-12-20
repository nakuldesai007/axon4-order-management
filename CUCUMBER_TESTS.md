# Cucumber Test Suite for Order Management System

This document describes the Cucumber BDD (Behavior Driven Development) test suite for the Axon 4 Order Management System.

## Overview

The Cucumber test suite provides comprehensive end-to-end testing of the order management system using BDD approach. The tests are written in Gherkin syntax and cover all major business scenarios.

## Test Structure

### Feature Files

The test scenarios are organized into the following feature files:

1. **`order_creation.feature`** - Tests for order creation functionality
   - Creating orders with valid customer information
   - Handling invalid customer data
   - Creating multiple orders for the same customer

2. **`order_item_management.feature`** - Tests for managing order items
   - Adding items to orders
   - Removing items from orders
   - Handling invalid item data
   - Managing multiple items

3. **`order_status_workflow.feature`** - Tests for order status transitions
   - Complete workflow from creation to shipment
   - Order cancellation scenarios
   - Invalid status transitions
   - Business rule validations

4. **`order_queries.feature`** - Tests for order query functionality
   - Retrieving orders by various criteria
   - Search and filtering operations
   - Statistical queries
   - Error handling for non-existent data

### Step Definitions

The step definitions are organized into corresponding Java classes:

- `OrderCreationSteps.java` - Handles order creation scenarios
- `OrderItemManagementSteps.java` - Handles item management scenarios
- `OrderStatusWorkflowSteps.java` - Handles status workflow scenarios
- `OrderQuerySteps.java` - Handles query scenarios

## Running the Tests

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Spring Boot 3.5.0

### Running All Cucumber Tests

```bash
mvn test -Dtest=CucumberTestRunner
```

### Running Specific Feature Files

```bash
# Run only order creation tests
mvn test -Dcucumber.features="src/test/resources/features/order_creation.feature"

# Run only order workflow tests
mvn test -Dcucumber.features="src/test/resources/features/order_status_workflow.feature"
```

### Running Tests with Tags

```bash
# Run tests with specific tags (if defined)
mvn test -Dcucumber.filter.tags="@smoke"
mvn test -Dcucumber.filter.tags="@regression"
```

## Test Reports

After running the tests, you can find the reports in:

- **HTML Report**: `target/cucumber-reports/cucumber.html`
- **JSON Report**: `target/cucumber-reports/cucumber.json`

## Test Configuration

### Spring Configuration

The tests use the `CucumberSpringConfiguration` class to configure Spring Boot test context:

```java
@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
```

### Test Profile

Tests run with the `test` profile, which uses:
- H2 in-memory database
- Test-specific configuration
- Isolated test environment

### Test Data Management

Each test scenario:
- Cleans up test data before execution using `@Before` hooks
- Creates necessary test data as part of the scenario setup
- Validates results against expected outcomes

## Business Scenarios Covered

### Order Creation
- ✅ Valid order creation with complete customer information
- ✅ Order creation with missing/invalid data
- ✅ Multiple orders for the same customer
- ✅ Order ID uniqueness validation

### Item Management
- ✅ Adding single and multiple items to orders
- ✅ Removing items from orders
- ✅ Quantity and price validations
- ✅ Error handling for non-existent orders/items

### Status Workflow
- ✅ Complete order lifecycle (CREATED → CONFIRMED → PROCESSED → SHIPPED)
- ✅ Order cancellation at various stages
- ✅ Invalid status transition handling
- ✅ Business rule enforcement

### Query Operations
- ✅ Order retrieval by ID
- ✅ Order filtering by customer, status, amount
- ✅ Search operations by customer name
- ✅ Statistical queries (count, average)
- ✅ Error handling for non-existent data

## Best Practices

### Writing New Scenarios

1. **Use descriptive scenario names** that clearly explain the business case
2. **Follow Given-When-Then structure** for clear test flow
3. **Use data tables** for testing multiple data sets
4. **Include both positive and negative test cases**
5. **Validate business rules** and error conditions

### Step Definition Guidelines

1. **Keep steps focused** on a single responsibility
2. **Use meaningful variable names** for test data
3. **Implement proper cleanup** in `@Before` hooks
4. **Handle exceptions appropriately** for error scenarios
5. **Use assertions** to validate expected outcomes

### Feature File Organization

1. **Group related scenarios** in the same feature file
2. **Use Background sections** for common setup
3. **Include business context** in feature descriptions
4. **Use tags** for test categorization (if needed)

## Troubleshooting

### Common Issues

1. **Test Context Not Loading**
   - Ensure `CucumberSpringConfiguration` is properly configured
   - Check that Spring Boot test dependencies are included

2. **Database Connection Issues**
   - Verify H2 database configuration in test profile
   - Check that test data cleanup is working properly

3. **Step Definition Not Found**
   - Ensure step definitions are in the correct package
   - Verify that the glue code path is correctly configured

4. **Test Failures**
   - Check test data setup and cleanup
   - Verify business logic implementation
   - Review error messages for debugging

### Debug Mode

To run tests in debug mode:

```bash
mvn test -Dtest=CucumberTestRunner -Dcucumber.options="--dry-run"
```

This will show which step definitions are missing without executing the tests.

## Integration with CI/CD

The Cucumber tests can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions configuration
- name: Run Cucumber Tests
  run: mvn test -Dtest=CucumberTestRunner
```

The tests will generate reports that can be published as artifacts in the CI/CD pipeline.

## Future Enhancements

Potential improvements for the test suite:

1. **Parallel Test Execution** - Configure parallel test execution for faster feedback
2. **Test Data Factories** - Create reusable test data factories
3. **API Testing** - Add REST API specific test scenarios
4. **Performance Testing** - Include performance test scenarios
5. **Visual Testing** - Add visual regression testing if UI is added
6. **Contract Testing** - Implement contract testing for service interactions 