#!/bin/bash
set -e

echo "🚀 Starting Axon Order Management System..."

# Build frontend
echo "📦 Building frontend..."
cd frontend
npm install
npm run build
cd ..

# Build and run Spring Boot with embedded frontend
echo "🔨 Building Spring Boot application..."
mvn clean package -DskipTests

echo "✅ Starting application on port 8080..."
echo "   Frontend: http://localhost:8080/"
echo "   API: http://localhost:8080/api/orders"
echo "   Swagger: http://localhost:8080/swagger-ui.html"
echo "   H2 Console: http://localhost:8080/h2-console"
echo ""

mvn spring-boot:run
