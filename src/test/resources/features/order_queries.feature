Feature: Order Queries
  As a customer service representative
  I want to search and retrieve order information
  So that I can assist customers with their orders

  Background:
    Given the order management system is running
    And multiple orders exist in the system

  Scenario: Retrieve order by ID
    Given an order with ID "ORDER-003" exists
    When I retrieve the order by ID "ORDER-003"
    Then the order should be returned
    And the order ID should be "ORDER-003"

  Scenario: Retrieve non-existent order by ID
    Given no order with ID "NON-EXISTENT" exists
    When I retrieve the order by ID "NON-EXISTENT"
    Then no order should be returned
    And a 404 error should be returned

  Scenario: Retrieve all orders
    When I retrieve all orders
    Then a list of orders should be returned
    And the list should not be empty

  Scenario: Retrieve orders by customer ID
    Given a customer with ID "CUST-003" has multiple orders
    When I retrieve orders for customer "CUST-003"
    Then a list of orders should be returned
    And all orders should belong to customer "CUST-003"

  Scenario: Retrieve orders by status
    Given there are orders with status "CONFIRMED"
    When I retrieve orders with status "CONFIRMED"
    Then a list of orders should be returned
    And all orders should have status "CONFIRMED"

  Scenario: Search orders by customer name
    Given there are orders for customer "John Smith"
    When I search orders by customer name "John"
    Then a list of orders should be returned
    And all orders should have customer names containing "John"

  Scenario: Retrieve orders by minimum amount
    Given there are orders with total amount greater than 100.00
    When I retrieve orders with minimum amount 100.00
    Then a list of orders should be returned
    And all orders should have total amount greater than or equal to 100.00

  Scenario: Get order count by status
    Given there are orders with status "CREATED"
    When I get the count of orders with status "CREATED"
    Then a count should be returned
    And the count should be greater than 0

  Scenario: Get average order value by status
    Given there are orders with status "CONFIRMED"
    When I get the average order value for status "CONFIRMED"
    Then an average value should be returned
    And the average should be greater than 0

  Scenario: Get average order value for status with no orders
    Given there are no orders with status "DELIVERED"
    When I get the average order value for status "DELIVERED"
    Then no average value should be returned
    And a 404 error should be returned 