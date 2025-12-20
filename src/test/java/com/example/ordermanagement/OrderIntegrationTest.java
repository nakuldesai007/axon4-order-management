package com.example.ordermanagement;

import com.example.ordermanagement.query.OrderSummaryRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderIntegrationTest {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private OrderSummaryRepository orderSummaryRepository;

    @Test
    void testOrderCreation() {
        // This test verifies that the application context loads and components are available
        assertNotNull(commandGateway);
        assertNotNull(orderSummaryRepository);
        
        // For now, just verify the application starts correctly
        // The actual command handling will be tested in a separate integration test
    }
} 