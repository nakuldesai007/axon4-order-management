# Aggregate Member Refactoring

## Overview

This document explains how event sourcing handlers can be moved to `@AggregateMember` classes in Axon Framework, demonstrating the refactoring applied to the Order aggregate.

## What is @AggregateMember?

`@AggregateMember` is an Axon Framework annotation that allows you to:
- **Decompose complex aggregates** into smaller, focused components
- **Improve code organization** by grouping related functionality
- **Handle events at the member level** for better encapsulation
- **Maintain single responsibility principle** within aggregate components

## Refactoring Applied

### Before: Monolithic Order Aggregate
The original `Order` class contained:
- All event sourcing handlers in one place
- Mixed responsibilities (items, status, customer info)
- Large, hard-to-maintain class

### After: Modular Design with Aggregate Members

#### 1. OrderItems Aggregate Member
```java
@AggregateMember
public class OrderItems {
    @EventSourcingHandler
    public void on(ItemAddedToOrderEvent event) { ... }
    
    @EventSourcingHandler
    public void on(ItemRemovedFromOrderEvent event) { ... }
    
    // Item-related business logic and state
}
```

#### 2. OrderStatusManager Aggregate Member
```java
@AggregateMember
public class OrderStatusManager {
    @EventSourcingHandler
    public void on(OrderConfirmedEvent event) { ... }
    
    @EventSourcingHandler
    public void on(OrderProcessedEvent event) { ... }
    
    // Status-related business logic and validation
}
```

#### 3. Simplified Order Aggregate
```java
@Aggregate
public class Order {
    @AggregateMember
    private OrderItems orderItems;
    
    @AggregateMember
    private OrderStatusManager statusManager;
    
    // Only handles order-level events and commands
}
```

## Benefits of This Approach

### 1. **Better Separation of Concerns**
- **OrderItems**: Handles all item-related logic and events
- **OrderStatusManager**: Manages order status transitions and validation
- **Order**: Coordinates between members and handles order-level operations

### 2. **Improved Maintainability**
- Each member has a single responsibility
- Easier to test individual components
- Clearer code organization

### 3. **Enhanced Reusability**
- Aggregate members can be reused in other aggregates
- Business logic is encapsulated and reusable

### 4. **Better Event Handling**
- Events are handled at the appropriate level
- Related events are grouped together
- Clearer event flow and state management

## Key Considerations

### 1. **Event Routing**
- Axon automatically routes events to the appropriate aggregate members
- Events are handled by the member that has the corresponding `@EventSourcingHandler`

### 2. **State Access**
- Aggregate members can access each other's state through the parent aggregate
- Command handlers in the main aggregate can delegate to members

### 3. **Initialization**
- Aggregate members must be initialized in the main aggregate's event sourcing handlers
- Typically done in the creation event handler

### 4. **Limitations**
- Not all event sourcing handlers can be moved to members
- Some events affect the entire aggregate and should remain in the main class
- Complex cross-member interactions need careful design

## Example: Event Flow

```
1. AddItemToOrderCommand → Order.handle()
2. Order validates and applies ItemAddedToOrderEvent
3. Axon routes ItemAddedToOrderEvent to OrderItems.on()
4. OrderItems updates its internal state
5. Order.getItems() delegates to OrderItems.getItems()
```

## Best Practices

1. **Group Related Functionality**: Put related events and state in the same member
2. **Keep Members Focused**: Each member should have a single, clear responsibility
3. **Maintain Clear Interfaces**: Provide clean getter methods for member state
4. **Handle Cross-Member Dependencies**: Design carefully when members need to interact
5. **Test Members Independently**: Each member should be testable in isolation

## Conclusion

Moving event sourcing handlers to `@AggregateMember` classes is a powerful technique for:
- **Improving code organization**
- **Enhancing maintainability**
- **Following domain-driven design principles**
- **Creating more modular and testable code**

This refactoring demonstrates how Axon Framework supports sophisticated aggregate design patterns while maintaining the benefits of event sourcing. 