package com.example.ordermanagement.event;

import java.time.LocalDateTime;

/**
 * Common interface for all domain events.
 * Provides consistent structure and common fields for all events.
 */
public interface DomainEvent {
    
    /**
     * Get the aggregate identifier (e.g., orderId)
     */
    String getAggregateId();
    
    /**
     * Get the timestamp when the event occurred
     */
    LocalDateTime getTimestamp();
    
    /**
     * Get the event type name
     */
    String getEventType();
}
