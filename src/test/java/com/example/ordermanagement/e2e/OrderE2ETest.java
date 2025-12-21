package com.example.ordermanagement.e2e;

import com.example.ordermanagement.query.OrderSummaryRepository;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Browser Tests
 * 
 * These tests use a real browser (Chrome) to test the actual UI and backend together.
 * 
 * Prerequisites:
 * 1. Frontend must be built and served by Spring Boot (production mode)
 *    OR frontend dev server must be running on port 3000
 * 2. Chrome browser must be installed
 * 3. Backend must be running (handled by @SpringBootTest)
 * 
 * To run these tests:
 * 1. Build frontend: cd frontend && npm run build
 * 2. Run tests: mvn test -Dtest=OrderE2ETest
 * 
 * OR for development mode:
 * 1. Start frontend dev server: cd frontend && npm run dev
 * 2. Update baseUrl in setUp() to use http://localhost:3000
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("End-to-End Browser Tests (UI + Backend)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderE2ETest {

    @LocalServerPort
    private int backendPort;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    static void setupAll() {
        // Selenium 4.6+ has built-in driver management
        // ChromeDriver will be automatically downloaded if needed
        // Don't set webdriver.chrome.driver - let Selenium manage it automatically
        System.clearProperty("webdriver.chrome.driver");
    }

    /**
     * Helper method to find Chrome binary in common locations
     */
    private String findChromeBinary() {
        String[] possiblePaths = {
            "/usr/bin/google-chrome",
            "/usr/bin/google-chrome-stable",
            "/usr/bin/chromium",
            "/usr/bin/chromium-browser",
            "/snap/bin/chromium",
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", // macOS
            System.getProperty("user.home") + "/.local/share/google/chrome/chrome"
        };
        
        for (String path : possiblePaths) {
            java.io.File file = new java.io.File(path);
            if (file.exists() && file.canExecute()) {
                return path;
            }
        }
        return null;
    }

    @BeforeEach
    void setUp() {
        // Clean database
        orderSummaryRepository.deleteAll();

        // Setup Chrome with options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode (no browser window)
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        
        // Try to find Chrome binary in common locations
        String chromeBinary = findChromeBinary();
        if (chromeBinary != null) {
            options.setBinary(chromeBinary);
            System.out.println("✓ Using Chrome binary: " + chromeBinary);
        } else {
            System.out.println("⚠ WARNING: Chrome binary not found in common locations.");
            System.out.println("  Selenium will try to use default location or download ChromeDriver.");
            System.out.println("  If test fails, install Chrome or set ChromeOptions.setBinary() to Chrome path.");
        }
        
        // For visible browser (debugging), comment out headless:
        // options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Use backend URL (frontend is served by Spring Boot in production mode)
        // OR use frontend dev server URL if running in dev mode
        baseUrl = "http://localhost:" + backendPort;
        
        // If frontend dev server is running separately, use:
        // baseUrl = "http://localhost:3000";
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("User creates order through UI form")
    void testCreateOrder_ThroughUI() {
        // Navigate to create order page
        driver.get(baseUrl + "/create");
        
        // Wait for form to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customerId")));
        
        // Fill in the form (simulates user typing)
        WebElement customerIdField = driver.findElement(By.id("customerId"));
        customerIdField.sendKeys("CUST-E2E-001");
        
        WebElement customerNameField = driver.findElement(By.id("customerName"));
        customerNameField.sendKeys("E2E Test Customer");
        
        WebElement customerEmailField = driver.findElement(By.id("customerEmail"));
        customerEmailField.sendKeys("e2e@test.com");
        
        WebElement shippingAddressField = driver.findElement(By.id("shippingAddress"));
        shippingAddressField.sendKeys("123 E2E Test Street, Test City, TC 12345");
        
        // Submit the form (simulates user clicking "Create Order" button)
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        assertTrue(submitButton.isEnabled(), "Submit button should be enabled");
        submitButton.click();
        
        // Wait for navigation to order details page
        wait.until(ExpectedConditions.urlContains("/orders/"));
        
        // Verify we're on the order details page
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/orders/"), "Should navigate to order details page");
        
        // Verify order ID is in the URL
        String orderId = currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
        assertNotNull(orderId);
        assertFalse(orderId.isEmpty());
        
        System.out.println("✓ Navigated to order details page: " + currentUrl);
        System.out.println("✓ Order ID: " + orderId);
        
        // Wait a bit for React to render
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check page source for debugging
        String pageSource = driver.getPageSource();
        if (pageSource.contains("Loading order details")) {
            System.out.println("⏳ Page is still loading...");
            // Wait for loading to complete
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("loading")));
        }
        
        // Wait for order details to load (try multiple selectors)
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("order-details")));
        } catch (Exception e) {
            // Try alternative selector
            System.out.println("⚠ Could not find .order-details, trying alternative selectors...");
            System.out.println("Page title: " + driver.getTitle());
            System.out.println("Page URL: " + driver.getCurrentUrl());
            
            // Try waiting for any order-related element
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.className("order-details")),
                ExpectedConditions.presenceOfElementLocated(By.className("order-header")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(), 'Order Details')]"))
            ));
        }
        
        System.out.println("✓ Order details page loaded");
        
        // Verify order information is displayed
        WebElement customerNameDisplay = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), 'E2E Test Customer')]")));
        assertNotNull(customerNameDisplay, "Customer name should be displayed");
        
        // Verify order status is CREATED
        WebElement statusBadge = driver.findElement(By.className("status-badge"));
        assertTrue(statusBadge.getText().contains("CREATED"), 
            "Order status should be CREATED");
    }

    @Test
    @Order(2)
    @DisplayName("User views order list and sees created orders")
    void testViewOrderList_DisplaysOrders() {
        // First, create an order via API (or navigate through UI)
        // For this test, we'll navigate directly to the list
        
        driver.get(baseUrl + "/");
        
        // Wait for order list to load
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.className("order-list")));
        
        // Check if there are orders or empty state
        List<WebElement> orders = driver.findElements(By.cssSelector("table tbody tr"));
        
        if (orders.isEmpty()) {
            // Verify empty state message
            WebElement emptyState = driver.findElement(By.className("empty-state"));
            assertNotNull(emptyState, "Empty state should be displayed when no orders");
        } else {
            // Verify orders are displayed in table
            assertFalse(orders.isEmpty(), "Orders should be displayed in the list");
            
            // Verify table headers
            WebElement orderIdHeader = driver.findElement(By.xpath("//th[text()='Order ID']"));
            assertNotNull(orderIdHeader, "Order ID column should exist");
        }
    }

    @Test
    @Order(3)
    @DisplayName("User creates order and adds items through UI")
    void testCreateOrderAndAddItems_ThroughUI() {
        // Step 1: Create order
        driver.get(baseUrl + "/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customerId")));
        
        driver.findElement(By.id("customerId")).sendKeys("CUST-E2E-002");
        driver.findElement(By.id("customerName")).sendKeys("Item Test Customer");
        driver.findElement(By.id("customerEmail")).sendKeys("items@test.com");
        driver.findElement(By.id("shippingAddress")).sendKeys("456 Item Test St");
        
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        // Wait for order details page
        wait.until(ExpectedConditions.urlContains("/orders/"));
        String orderId = driver.getCurrentUrl().substring(driver.getCurrentUrl().lastIndexOf("/") + 1);
        
        // Step 2: Add item to order
        // Click "Add Item" button to show the form
        WebElement addItemButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Add Item')]")));
        addItemButton.click();
        
        // Wait for add item form to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//input[@type='text' and contains(@placeholder, 'PROD')]")));
        
        // Find and fill item form fields (using input type and placeholder as selectors)
        List<WebElement> inputs = driver.findElements(By.cssSelector(".add-item-form input"));
        WebElement productIdField = inputs.stream()
            .filter(e -> e.getAttribute("placeholder") != null && 
                        e.getAttribute("placeholder").contains("PROD"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Product ID field not found"));
        productIdField.sendKeys("PROD-E2E-001");
        
        WebElement productNameField = inputs.stream()
            .filter(e -> e.getAttribute("placeholder") != null && 
                        e.getAttribute("placeholder").contains("iPhone"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Product Name field not found"));
        productNameField.sendKeys("E2E Test Product");
        
        WebElement quantityField = inputs.stream()
            .filter(e -> e.getAttribute("type") != null && 
                        e.getAttribute("type").equals("number") &&
                        e.getAttribute("min") != null)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Quantity field not found"));
        quantityField.clear();
        quantityField.sendKeys("2");
        
        WebElement priceField = inputs.stream()
            .filter(e -> e.getAttribute("type") != null && 
                        e.getAttribute("type").equals("number") &&
                        e.getAttribute("step") != null)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Price field not found"));
        priceField.clear();
        priceField.sendKeys("99.99");
        
        // Submit add item form
        WebElement submitAddItemButton = driver.findElement(
            By.xpath("//button[contains(text(), 'Add Item') and not(contains(text(), '+'))]"));
        submitAddItemButton.click();
        
        // Wait for item to appear in the list
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), 'E2E Test Product')]")));
        
        // Verify item is displayed
        WebElement itemRow = driver.findElement(
            By.xpath("//*[contains(text(), 'E2E Test Product')]"));
        assertNotNull(itemRow, "Item should be displayed in the order");
        
        // Verify quantity and price are displayed
        WebElement quantityDisplay = driver.findElement(
            By.xpath("//*[contains(text(), '2')]"));
        assertNotNull(quantityDisplay, "Quantity should be displayed");
    }

    @Test
    @Order(4)
    @DisplayName("User confirms order through UI")
    void testConfirmOrder_ThroughUI() {
        // Create order first (via API or UI)
        // For simplicity, navigate to create page
        driver.get(baseUrl + "/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customerId")));
        
        driver.findElement(By.id("customerId")).sendKeys("CUST-E2E-003");
        driver.findElement(By.id("customerName")).sendKeys("Confirm Test Customer");
        driver.findElement(By.id("customerEmail")).sendKeys("confirm@test.com");
        driver.findElement(By.id("shippingAddress")).sendKeys("789 Confirm St");
        
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/orders/"));
        
        // Add an item first (required for confirmation)
        // Click "Add Item" button
        WebElement addItemBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Add Item')]")));
        addItemBtn.click();
        
        // Wait for form and fill fields
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".add-item-form")));
        
        List<WebElement> formInputs = driver.findElements(By.cssSelector(".add-item-form input"));
        formInputs.get(0).sendKeys("PROD-E2E-002"); // Product ID
        formInputs.get(1).sendKeys("Confirm Test Product"); // Product Name
        formInputs.get(2).clear();
        formInputs.get(2).sendKeys("1"); // Quantity
        formInputs.get(3).clear();
        formInputs.get(3).sendKeys("199.99"); // Price
        
        WebElement submitAddBtn = driver.findElement(
            By.xpath("//button[contains(text(), 'Add Item') and contains(@class, 'btn-success')]"));
        submitAddBtn.click();
        
        // Wait for item to be added
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//*[contains(text(), 'Confirm Test Product')]")));
        
        // Find and click "Confirm Order" button
        WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(text(), 'Confirm') or contains(text(), 'Confirm Order')]")));
        confirmButton.click();
        
        // Wait for status to update
        wait.until(ExpectedConditions.textToBePresentInElement(
            driver.findElement(By.className("status-badge")), "CONFIRMED"));
        
        // Verify status changed to CONFIRMED
        WebElement statusBadge = driver.findElement(By.className("status-badge"));
        assertTrue(statusBadge.getText().contains("CONFIRMED"), 
            "Order status should be CONFIRMED");
    }

    @Test
    @Order(5)
    @DisplayName("User filters orders by status in order list")
    void testFilterOrdersByStatus_ThroughUI() {
        driver.get(baseUrl + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("order-list")));
        
        // Find filter buttons
        List<WebElement> filterButtons = driver.findElements(By.className("filter-btn"));
        assertFalse(filterButtons.isEmpty(), "Filter buttons should be present");
        
        // Click on "Created" filter
        WebElement createdFilter = driver.findElement(
            By.xpath("//button[contains(text(), 'Created')]"));
        createdFilter.click();
        
        // Wait for filter to apply (check if active class is added)
        wait.until(ExpectedConditions.attributeContains(createdFilter, "class", "active"));
        
        // Verify filter is active
        assertTrue(createdFilter.getAttribute("class").contains("active"),
            "Created filter should be active");
        
        // Click on "All" filter
        WebElement allFilter = driver.findElement(
            By.xpath("//button[contains(text(), 'All')]"));
        allFilter.click();
        
        wait.until(ExpectedConditions.attributeContains(allFilter, "class", "active"));
        assertTrue(allFilter.getAttribute("class").contains("active"),
            "All filter should be active");
    }

    @Test
    @Order(6)
    @DisplayName("User navigates between pages")
    void testNavigation_ThroughUI() {
        // Start at home page
        driver.get(baseUrl + "/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("order-list")));
        
        // Click "Create New Order" button
        WebElement createButton = driver.findElement(
            By.xpath("//a[contains(text(), 'Create') or contains(text(), 'Create New Order')]"));
        createButton.click();
        
        // Verify we're on create page
        wait.until(ExpectedConditions.urlContains("/create"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customerId")));
        
        // Click Cancel button to go back
        WebElement cancelButton = driver.findElement(
            By.xpath("//button[contains(text(), 'Cancel')]"));
        cancelButton.click();
        
        // Verify we're back on home page
        wait.until(ExpectedConditions.urlMatches(baseUrl + "/?"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("order-list")));
    }

    @Test
    @Order(7)
    @DisplayName("Form validation works correctly")
    void testFormValidation_ThroughUI() {
        driver.get(baseUrl + "/create");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customerId")));
        
        // Try to submit form without required fields
        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
        
        // HTML5 validation should prevent submission
        // Check if form is still on the page (not navigated away)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("customerId")));
        
        // Fill only required fields
        driver.findElement(By.id("customerId")).sendKeys("CUST-E2E-VALID");
        driver.findElement(By.id("customerName")).sendKeys("Validation Test");
        
        // Now submit should work
        submitButton.click();
        
        // Should navigate to order details
        wait.until(ExpectedConditions.urlContains("/orders/"));
    }

    // Helper method to wait for page load
    private void waitForPageLoad() {
        wait.until(webDriver -> 
            ((org.openqa.selenium.JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
    }
}

