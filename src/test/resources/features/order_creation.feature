Feature: Order Creation
  As a customer
  I want to create new orders
  So that I can purchase products

  Background:
    Given the order management system is running

  Scenario: Create a new order with valid customer information
    Given a customer with ID "CUST-001" and name "John Doe"
    And the customer email is "john.doe@example.com"
    And the shipping address is "123 Main St, City, State 12345"
    When I create a new order for this customer
    Then the order should be created successfully
    And the order status should be "CREATED"
    And the order should have no items

  Scenario: Create an order with missing customer information
    Given a customer with ID "" and name ""
    When I create a new order for this customer
    Then the order creation should fail
    And an error message should be returned

  Scenario: Create multiple orders for the same customer
    Given a customer with ID "CUST-002" and name "Jane Smith"
    And the customer email is "jane.smith@example.com"
    And the shipping address is "456 Oak Ave, City, State 12345"
    When I create a new order for this customer
    And I create another new order for this customer
    Then both orders should be created successfully
    And both orders should have different IDs
    And both orders should have status "CREATED" 