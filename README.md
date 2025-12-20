# Axon 4 Order Management System

A comprehensive e-commerce order management system built with **Axon Framework 4.11.1** and **Spring Boot 3.5.0**, demonstrating Event Sourcing and CQRS patterns.

## 🚀 Features

- **Event Sourcing**: Complete event sourcing implementation with persistent event store
- **CQRS**: Command Query Responsibility Segregation with separate read/write models
- **Order Lifecycle Management**: Full order lifecycle from creation to delivery
- **RESTful API**: Comprehensive REST endpoints for all order operations
- **Real-time Event Handling**: Event-driven architecture with immediate consistency
- **H2 Database**: In-memory database with console access
- **OpenAPI Documentation**: Auto-generated API documentation
- **Health Monitoring**: Spring Boot Actuator endpoints

## 🏗️ Architecture

### Domain Model
- **Order Aggregate**: Core business logic with validation rules
- **Commands**: Immutable command objects for state changes
- **Events**: Immutable event objects representing state changes
- **Event Handlers**: Projection handlers updating the query model

### Order Lifecycle
1. **CREATED** → Order is created with customer information
2. **CONFIRMED** → Order is confirmed (requires items)
3. **PROCESSED** → Order is being processed
4. **SHIPPED** → Order is shipped with tracking number
5. **DELIVERED** → Order is delivered (future enhancement)
6. **CANCELLED** → Order is cancelled (cannot cancel shipped/delivered)

## 📁 Project Structure

```
src/main/java/com/example/ordermanagement/
├── aggregate/
│   └── Order.java                    # Main aggregate with business logic
├── command/
│   ├── CreateOrderCommand.java       # Create order command
│   ├── AddItemToOrderCommand.java    # Add item command
│   ├── RemoveItemFromOrderCommand.java # Remove item command
│   ├── ConfirmOrderCommand.java      # Confirm order command
│   ├── ProcessOrderCommand.java      # Process order command
│   ├── ShipOrderCommand.java         # Ship order command
│   └── CancelOrderCommand.java       # Cancel order command
├── event/
│   ├── OrderCreatedEvent.java        # Order created event
│   ├── ItemAddedToOrderEvent.java    # Item added event
│   ├── ItemRemovedFromOrderEvent.java # Item removed event
│   ├── OrderConfirmedEvent.java      # Order confirmed event
│   ├── OrderProcessedEvent.java      # Order processed event
│   ├── OrderShippedEvent.java        # Order shipped event
│   └── OrderCancelledEvent.java      # Order cancelled event
├── query/
│   ├── OrderSummary.java             # Query model entity
│   ├── OrderItemSummary.java         # Order item entity
│   └── OrderSummaryRepository.java   # Query model repository
├── handler/
│   └── OrderEventHandler.java        # Event handlers for projections
├── controller/
│   └── OrderController.java          # REST API controller
└── OrderManagementApplication.java   # Main application class
```

## 🛠️ Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher

## 🚀 Getting Started

### 1. Clone and Build

```bash
cd axon4-order-management
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Access H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## 📚 API Documentation

### Create Order
```bash
POST /api/orders
Content-Type: application/json

{
  "customerId": "CUST-001",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "shippingAddress": "123 Main St, City, State 12345"
}
```

### Add Item to Order
```bash
POST /api/orders/{orderId}/items
Content-Type: application/json

{
  "productId": "PROD-001",
  "productName": "iPhone 15 Pro",
  "quantity": 1,
  "price": 999.99
}
```

### Remove Item from Order
```bash
DELETE /api/orders/{orderId}/items/{productId}
```

### Confirm Order
```bash
POST /api/orders/{orderId}/confirm
```

### Process Order
```bash
POST /api/orders/{orderId}/process
```

### Ship Order
```bash
POST /api/orders/{orderId}/ship
Content-Type: application/json

{
  "trackingNumber": "TRK123456789"
}
```

### Cancel Order
```bash
POST /api/orders/{orderId}/cancel
Content-Type: application/json

{
  "reason": "Customer requested cancellation"
}
```

### Query Operations

#### Get Order by ID
```bash
GET /api/orders/{orderId}
```

#### Get All Orders
```bash
GET /api/orders
```

#### Get Orders by Customer
```bash
GET /api/orders/customer/{customerId}
```

#### Get Orders by Status
```bash
GET /api/orders/status/{status}
```

#### Search Orders by Customer Name
```bash
GET /api/orders/search?customerName=John
```

#### Get Orders by Minimum Amount
```bash
GET /api/orders/min-amount/{minAmount}
```

#### Get Statistics
```bash
# Order count by status
GET /api/orders/statistics/status/{status}/count

# Average order value by status
GET /api/orders/statistics/status/{status}/average
```

## 🔧 Configuration

### Key Configuration Options

```yaml
axon:
  eventhandling:
    processors:
      default:
        mode: subscribing        # Event processing mode
        source: eventStore       # Event source
  serializer:
    general: jackson            # Serialization format
    events: jackson
    messages: jackson

spring:
  jpa:
    hibernate:
      ddl-auto: create-drop     # Database schema management
    show-sql: true              # SQL logging
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Integration Test Example
```bash
# Create an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "shippingAddress": "123 Main St"
  }'

# Add items
curl -X POST http://localhost:8080/api/orders/{orderId}/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "productName": "iPhone 15",
    "quantity": 1,
    "price": 999.99
  }'

# Confirm order
curl -X POST http://localhost:8080/api/orders/{orderId}/confirm
```

## 🔍 Monitoring

### Health Check
```bash
GET /actuator/health
```

### Metrics
```bash
GET /actuator/metrics
```

### Application Info
```bash
GET /actuator/info
```

## 🏭 Production Considerations

For production deployment, consider:

1. **Database**: Replace H2 with PostgreSQL, MySQL, or MongoDB
2. **Event Store**: Configure persistent event store
3. **Security**: Add authentication and authorization
4. **Monitoring**: Integrate with monitoring tools (Prometheus, Grafana)
5. **Message Broker**: Use Kafka or RabbitMQ for event distribution
6. **Caching**: Add Redis for query model caching
7. **Load Balancing**: Configure load balancers for scalability

## 📊 Event Sourcing Benefits

- **Audit Trail**: Complete history of all changes
- **Temporal Queries**: Query system state at any point in time
- **Event Replay**: Rebuild read models from events
- **Debugging**: Trace exact sequence of events
- **Scalability**: Separate read/write concerns

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For questions and support:
- Create an issue in the repository
- Check the Axon Framework documentation
- Review Spring Boot documentation

---

**Built with ❤️ using Axon Framework 4 and Spring Boot 3.5.0** 