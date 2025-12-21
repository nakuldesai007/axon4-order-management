# End-to-End Browser Testing Guide

This guide explains how to run browser-based E2E tests that test both the UI and backend together using a real browser.

## Prerequisites

1. **Chrome Browser** - Must be installed on your system
2. **Frontend Built** - Frontend must be built and served by Spring Boot
   OR frontend dev server running on port 3000
3. **Backend Running** - Handled automatically by `@SpringBootTest`

## Setup

### Option 1: Production Mode (Frontend served by Spring Boot)

1. Build the frontend:
   ```bash
   cd frontend
   npm install
   npm run build
   ```

2. Run the E2E tests:
   ```bash
   mvn test -Dtest=OrderE2ETest
   ```

The tests will use `http://localhost:{randomPort}` where Spring Boot serves both backend and frontend.

### Option 2: Development Mode (Frontend dev server)

1. Start frontend dev server:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Frontend will run on `http://localhost:3000`

2. Update `OrderE2ETest.java`:
   ```java
   // In setUp() method, change:
   baseUrl = "http://localhost:3000";
   ```

3. Run the E2E tests:
   ```bash
   mvn test -Dtest=OrderE2ETest
   ```

## Test Scenarios

The `OrderE2ETest` class includes the following browser-based tests:

1. **testCreateOrder_ThroughUI** - User fills form and creates order
2. **testViewOrderList_DisplaysOrders** - User views order list
3. **testCreateOrderAndAddItems_ThroughUI** - User creates order and adds items
4. **testConfirmOrder_ThroughUI** - User confirms order through UI
5. **testFilterOrdersByStatus_ThroughUI** - User filters orders by status
6. **testNavigation_ThroughUI** - User navigates between pages
7. **testFormValidation_ThroughUI** - Form validation works correctly

## Running Tests

### Run All E2E Tests
```bash
mvn test -Dtest=OrderE2ETest
```

### Run Specific Test
```bash
mvn test -Dtest=OrderE2ETest#testCreateOrder_ThroughUI
```

### Run with Visible Browser (for debugging)
Edit `OrderE2ETest.java` and comment out headless mode:
```java
// ChromeOptions options = new ChromeOptions();
// options.addArguments("--headless"); // Comment this out
```

## Troubleshooting

### ChromeDriver Issues
If you get ChromeDriver errors, WebDriverManager will automatically download the correct version. Make sure you have internet access.

### Frontend Not Loading
- Ensure frontend is built: `cd frontend && npm run build`
- Or ensure frontend dev server is running: `cd frontend && npm run dev`
- Check the `baseUrl` in the test matches your setup

### Tests Timing Out
- Increase wait time in `WebDriverWait` constructor
- Check if backend is responding: `curl http://localhost:8080/api/orders`
- Check browser console for JavaScript errors

### Element Not Found
- Use browser developer tools to inspect actual element IDs/classes
- Update selectors in test to match actual UI
- Add explicit waits for dynamic content

## Best Practices

1. **Use Explicit Waits** - Always wait for elements to be present before interacting
2. **Clean State** - Each test starts with a clean database
3. **Isolated Tests** - Tests should be independent and runnable in any order
4. **Meaningful Assertions** - Verify actual user-visible outcomes
5. **Headless Mode** - Use headless for CI/CD, visible for debugging

## CI/CD Integration

For CI/CD pipelines, ensure:
- Chrome/Chromium is installed
- Frontend is built before running tests
- Tests run in headless mode (default)

Example GitHub Actions:
```yaml
- name: Build Frontend
  run: |
    cd frontend
    npm install
    npm run build

- name: Run E2E Tests
  run: mvn test -Dtest=OrderE2ETest
```

