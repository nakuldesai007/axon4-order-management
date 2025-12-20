Feature: Order Item Management
  As a customer
  I want to add and remove items from my orders
  So that I can customize my purchase

  Background:
    Given the order management system is running
    And an order exists with ID "ORDER-001" for customer "CUST-001"

  Scenario: Add a single item to an order
    Given a product with ID "PROD-001" and name "iPhone 15 Pro"
    And the product price is 999.99
    And the quantity is 1
    When I add this item to the order
    Then the item should be added successfully
    And the order should contain 1 item
    And the order total should be 999.99

  Scenario: Add multiple items to an order
    Given a product with ID "PROD-002" and name "AirPods Pro"
    And the product price is 249.99
    And the quantity is 2
    When I add this item to the order
    And I add another product with ID "PROD-003" and name "iPhone Case"
    And the product price is 49.99
    And the quantity is 1
    Then the order should contain 3 items
    And the order total should be 1299.97

  Scenario: Add item with invalid quantity
    Given a product with ID "PROD-004" and name "Test Product"
    And the product price is 100.00
    And the quantity is 0
    When I add this item to the order
    Then the item addition should fail
    And an error message should be returned

  Scenario: Remove an item from an order
    Given the order contains a product with ID "PROD-001"
    When I remove this item from the order
    Then the item should be removed successfully
    And the order should not contain the item

  Scenario: Remove non-existent item from an order
    Given the order does not contain a product with ID "PROD-999"
    When I remove this item from the order
    Then the item removal should fail
    And an error message should be returned

  Scenario: Add item to non-existent order
    Given a product with ID "PROD-005" and name "Test Product"
    And the product price is 50.00
    And the quantity is 1
    When I add this item to order "NON-EXISTENT-ORDER"
    Then the item addition should fail
    And an error message should be returned 