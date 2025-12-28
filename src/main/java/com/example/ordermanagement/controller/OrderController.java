package com.example.ordermanagement.controller;

import com.example.ordermanagement.command.*;
import com.example.ordermanagement.query.OrderSummary;
import com.example.ordermanagement.query.OrderSummaryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Order Management", description = "APIs for managing e-commerce orders using CQRS and Event Sourcing")
public class OrderController {

    private final CommandGateway commandGateway;
    private final OrderSummaryRepository orderSummaryRepository;

    public OrderController(CommandGateway commandGateway, OrderSummaryRepository orderSummaryRepository) {
        this.commandGateway = commandGateway;
        this.orderSummaryRepository = orderSummaryRepository;
    }

    @PostMapping
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order with customer information and returns the order ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order created successfully",
            content = @Content(mediaType = "text/plain", schema = @Schema(example = "550e8400-e29b-41d4-a716-446655440000"))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public CompletableFuture<String> createOrder(
        @Parameter(description = "Order creation request", required = true)
        @RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        
        CreateOrderCommand command = new CreateOrderCommand(
                orderId,
                request.getCustomerId(),
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getShippingAddress()
        );
        
        return commandGateway.send(command);
    }

    @PostMapping("/{orderId}/items")
    @Operation(
        summary = "Add item to order",
        description = "Adds a product item to an existing order"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order cannot be modified in current status")
    })
    public CompletableFuture<Void> addItemToOrder(
            @Parameter(description = "Order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String orderId,
            @Parameter(description = "Item to add", required = true)
            @RequestBody AddItemRequest request) {
        
        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId,
                request.getProductId(),
                request.getProductName(),
                request.getQuantity(),
                request.getPrice()
        );
        
        return commandGateway.send(command);
    }

    @DeleteMapping("/{orderId}/items/{productId}")
    @Operation(
        summary = "Remove item from order",
        description = "Removes a product item from an existing order"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item removed successfully"),
        @ApiResponse(responseCode = "404", description = "Order or item not found"),
        @ApiResponse(responseCode = "409", description = "Order cannot be modified in current status")
    })
    public CompletableFuture<Void> removeItemFromOrder(
            @Parameter(description = "Order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String orderId,
            @Parameter(description = "Product ID to remove", required = true, example = "PROD-001")
            @PathVariable String productId) {
        
        RemoveItemFromOrderCommand command = new RemoveItemFromOrderCommand(orderId, productId);
        return commandGateway.send(command);
    }

    @PostMapping("/{orderId}/confirm")
    @Operation(
        summary = "Confirm order",
        description = "Confirms an order, changing its status from CREATED to CONFIRMED"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order confirmed successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order cannot be confirmed in current status")
    })
    public CompletableFuture<Void> confirmOrder(
            @Parameter(description = "Order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String orderId) {
        ConfirmOrderCommand command = new ConfirmOrderCommand(orderId);
        return commandGateway.send(command);
    }

    @PostMapping("/{orderId}/process")
    @Operation(
        summary = "Process order",
        description = "Processes an order, changing its status from CONFIRMED to PROCESSED"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order processed successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order cannot be processed in current status")
    })
    public CompletableFuture<Void> processOrder(
            @Parameter(description = "Order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String orderId) {
        ProcessOrderCommand command = new ProcessOrderCommand(orderId);
        return commandGateway.send(command);
    }

    @PostMapping("/{orderId}/ship")
    @Operation(
        summary = "Ship order",
        description = "Ships an order, changing its status from PROCESSED to SHIPPED"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order shipped successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order cannot be shipped in current status")
    })
    public CompletableFuture<Void> shipOrder(
            @Parameter(description = "Order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String orderId,
            @Parameter(description = "Shipping information", required = true)
            @RequestBody ShipOrderRequest request) {
        
        ShipOrderCommand command = new ShipOrderCommand(orderId, request.getTrackingNumber());
        return commandGateway.send(command);
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(
        summary = "Cancel order",
        description = "Cancels an order, changing its status to CANCELLED"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "409", description = "Order cannot be cancelled in current status")
    })
    public CompletableFuture<Void> cancelOrder(
            @Parameter(description = "Order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String orderId,
            @Parameter(description = "Cancellation reason", required = true)
            @RequestBody CancelOrderRequest request) {
        
        CancelOrderCommand command = new CancelOrderCommand(orderId, request.getReason());
        return commandGateway.send(command);
    }

    @PutMapping("/{orderId}/shipping-address")
    @Operation(
            summary = "Update shipping address",
            description = "Updates the shipping address for an order that has not yet been shipped"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipping address updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order cannot be modified in current status")
    })
    public CompletableFuture<Void> updateShippingAddress(
            @Parameter(description = "Order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String orderId,
            @Parameter(description = "New shipping address", required = true)
            @RequestBody UpdateShippingAddressRequest request) {

        UpdateShippingAddressCommand command = new UpdateShippingAddressCommand(
                orderId,
                request.getShippingAddress()
        );

        return commandGateway.send(command);
    }

    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves a specific order by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummary.class))),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderSummary> getOrder(
            @Parameter(description = "Order ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String orderId) {
        return orderSummaryRepository.findById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
        summary = "Get all orders",
        description = "Retrieves all orders in the system"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummary.class)))
    })
    public List<OrderSummary> getAllOrders() {
        return orderSummaryRepository.findAll();
    }

    @GetMapping("/customer/{customerId}")
    @Operation(
        summary = "Get orders by customer",
        description = "Retrieves all orders for a specific customer"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummary.class)))
    })
    public List<OrderSummary> getOrdersByCustomer(
            @Parameter(description = "Customer ID", required = true, example = "CUST-001")
            @PathVariable String customerId) {
        return orderSummaryRepository.findByCustomerId(customerId);
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get orders by status",
        description = "Retrieves all orders with a specific status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummary.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status value")
    })
    public List<OrderSummary> getOrdersByStatus(
            @Parameter(description = "Order status", required = true, example = "CREATED", 
                      schema = @Schema(allowableValues = {"CREATED", "CONFIRMED", "PROCESSED", "SHIPPED", "DELIVERED", "CANCELLED"}))
            @PathVariable String status) {
        OrderSummary.OrderStatus orderStatus = OrderSummary.OrderStatus.valueOf(status.toUpperCase());
        return orderSummaryRepository.findByStatus(orderStatus);
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search orders by customer name",
        description = "Searches for orders by customer name (partial match)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummary.class)))
    })
    public List<OrderSummary> searchOrdersByCustomerName(
            @Parameter(description = "Customer name to search for", required = true, example = "John")
            @RequestParam String customerName) {
        return orderSummaryRepository.findByCustomerNameContaining(customerName);
    }

    @GetMapping("/min-amount/{minAmount}")
    @Operation(
        summary = "Get orders by minimum amount",
        description = "Retrieves all orders with a total amount greater than or equal to the specified value"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderSummary.class)))
    })
    public List<OrderSummary> getOrdersByMinAmount(
            @Parameter(description = "Minimum order amount", required = true, example = "100.00")
            @PathVariable BigDecimal minAmount) {
        return orderSummaryRepository.findByTotalAmountGreaterThanEqual(minAmount);
    }

    @GetMapping("/statistics/status/{status}/count")
    @Operation(
        summary = "Get order count by status",
        description = "Returns the count of orders with a specific status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(example = "5")))
    })
    public long getOrderCountByStatus(
            @Parameter(description = "Order status", required = true, example = "CREATED",
                      schema = @Schema(allowableValues = {"CREATED", "CONFIRMED", "PROCESSED", "SHIPPED", "DELIVERED", "CANCELLED"}))
            @PathVariable String status) {
        OrderSummary.OrderStatus orderStatus = OrderSummary.OrderStatus.valueOf(status.toUpperCase());
        return orderSummaryRepository.countByStatus(orderStatus);
    }

    @GetMapping("/statistics/status/{status}/average")
    @Operation(
        summary = "Get average order value by status",
        description = "Returns the average order value for orders with a specific status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Average value retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(example = "150.75"))),
        @ApiResponse(responseCode = "404", description = "No orders found with the specified status")
    })
    public ResponseEntity<BigDecimal> getAverageOrderValueByStatus(
            @Parameter(description = "Order status", required = true, example = "CONFIRMED",
                      schema = @Schema(allowableValues = {"CREATED", "CONFIRMED", "PROCESSED", "SHIPPED", "DELIVERED", "CANCELLED"}))
            @PathVariable String status) {
        OrderSummary.OrderStatus orderStatus = OrderSummary.OrderStatus.valueOf(status.toUpperCase());
        return orderSummaryRepository.getAverageOrderValueByStatus(orderStatus)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Request/Response DTOs
    @Schema(description = "Request to create a new order")
    public static class CreateOrderRequest {
        @Schema(description = "Customer ID", example = "CUST-001", required = true)
        private String customerId;
        
        @Schema(description = "Customer name", example = "John Doe", required = true)
        private String customerName;
        
        @Schema(description = "Customer email", example = "john.doe@example.com")
        private String customerEmail;
        
        @Schema(description = "Shipping address", example = "123 Main St, City, State 12345")
        private String shippingAddress;

        // Getters and Setters
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    }

    @Schema(description = "Request to add an item to an order")
    public static class AddItemRequest {
        @Schema(description = "Product ID", example = "PROD-001", required = true)
        private String productId;
        
        @Schema(description = "Product name", example = "iPhone 15 Pro", required = true)
        private String productName;
        
        @Schema(description = "Quantity", example = "1", minimum = "1", required = true)
        private int quantity;
        
        @Schema(description = "Product price", example = "999.99", minimum = "0.01", required = true)
        private BigDecimal price;

        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    @Schema(description = "Request to ship an order")
    public static class ShipOrderRequest {
        @Schema(description = "Tracking number", example = "TRK123456789", required = true)
        private String trackingNumber;

        // Getters and Setters
        public String getTrackingNumber() { return trackingNumber; }
        public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    }

    @Schema(description = "Request to cancel an order")
    public static class CancelOrderRequest {
        @Schema(description = "Cancellation reason", example = "Customer requested cancellation", required = true)
        private String reason;

        // Getters and Setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    @Schema(description = "Request to update the shipping address of an order")
    public static class UpdateShippingAddressRequest {
        @Schema(description = "The new shipping address", example = "456 Oak Ave, Town, State 67890", required = true)
        private String shippingAddress;

        // Getters and Setters
        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }
    }
}
