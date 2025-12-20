Feature: Order Status Workflow
  As a customer service representative
  I want to manage order status transitions
  So that orders can be processed through the fulfillment pipeline

  Background:
    Given the order management system is running
    And an order exists with ID "ORDER-002" for customer "CUST-002"
    And the order contains items

  Scenario: Complete order workflow from creation to shipment
    Given an order with status "CREATED"
    When I confirm the order
    Then the order status should be "CONFIRMED"
    When I process the order
    Then the order status should be "PROCESSED"
    When I ship the order with tracking number "TRK123456789"
    Then the order status should be "SHIPPED"
    And the order should have tracking number "TRK123456789"

  Scenario: Cancel an order in CREATED status
    Given an order with status "CREATED"
    When I cancel the order with reason "Customer requested cancellation"
    Then the order status should be "CANCELLED"
    And the cancellation reason should be "Customer requested cancellation"

  Scenario: Cancel an order in CONFIRMED status
    Given an order with status "CONFIRMED"
    When I cancel the order with reason "Out of stock"
    Then the order status should be "CANCELLED"
    And the cancellation reason should be "Out of stock"

  Scenario: Attempt to cancel a shipped order
    Given an order with status "SHIPPED"
    When I attempt to cancel the order
    Then the cancellation should fail
    And an error message should be returned

  Scenario: Attempt to confirm an already confirmed order
    Given an order with status "CONFIRMED"
    When I attempt to confirm the order again
    Then the confirmation should fail
    And an error message should be returned

  Scenario: Attempt to process an unconfirmed order
    Given an order with status "CREATED"
    When I attempt to process the order
    Then the processing should fail
    And an error message should be returned

  Scenario: Attempt to ship an unprocessed order
    Given an order with status "CONFIRMED"
    When I attempt to ship the order
    Then the shipment should fail
    And an error message should be returned

  Scenario: Ship order without tracking number
    Given an order with status "PROCESSED"
    When I attempt to ship the order without tracking number
    Then the shipment should fail
    And an error message should be returned 