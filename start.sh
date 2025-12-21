#!/bin/bash
set -e

MODE=${1:-production}

if [ "$MODE" = "dev" ]; then
    echo "🚀 Starting Axon Order Management System in DEVELOPMENT mode..."
    echo ""
    echo "📦 Starting backend on port 8080..."
    echo "📦 Starting frontend on port 3000 (with hot reload)..."
    echo ""
    echo "   Frontend: http://localhost:3000/"
    echo "   API: http://localhost:8080/api/orders"
    echo "   Swagger: http://localhost:8080/swagger-ui.html"
    echo "   H2 Console: http://localhost:8080/h2-console"
    echo ""
    echo "Press Ctrl+C to stop both servers"
    echo ""
    
    # Function to cleanup on exit
    cleanup() {
        echo ""
        echo "🛑 Stopping servers..."
        kill $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
        # Unset trap before exit to prevent infinite recursion
        trap - INT TERM EXIT
        exit
    }
    
    # Set trap BEFORE starting any processes to ensure cleanup on failure
    trap cleanup INT TERM EXIT
    
    # Start backend in background
    mvn spring-boot:run &
    BACKEND_PID=$!
    
    # Wait for backend to be ready
    echo "⏳ Waiting for backend to start..."
    sleep 5
    
    # Check if backend is running
    if ! kill -0 $BACKEND_PID 2>/dev/null; then
        echo "❌ Backend failed to start"
        exit 1
    fi
    
    # Start frontend
    cd frontend
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        echo "   Installing frontend dependencies..."
        npm install
    fi
    npm run dev &
    FRONTEND_PID=$!
    
    # Wait for both processes
    wait
    
elif [ "$MODE" = "production" ]; then
    echo "🚀 Starting Axon Order Management System in PRODUCTION mode..."
    
    # Build frontend
    echo "📦 Building frontend..."
    cd frontend
    if [ ! -d "node_modules" ]; then
        echo "   Installing frontend dependencies..."
        npm install
    fi
    npm run build
    cd ..
    
    # Build and run Spring Boot with embedded frontend
    echo "🔨 Building Spring Boot application..."
    mvn clean package -DskipTests
    
    echo ""
    echo "✅ Starting application on port 8080..."
    echo "   Frontend: http://localhost:8080/"
    echo "   API: http://localhost:8080/api/orders"
    echo "   Swagger: http://localhost:8080/swagger-ui.html"
    echo "   H2 Console: http://localhost:8080/h2-console"
    echo ""
    
    mvn spring-boot:run
    
else
    echo "Usage: ./start.sh [dev|production]"
    echo ""
    echo "  dev        - Run in development mode (separate frontend/backend with hot reload)"
    echo "  production - Run in production mode (built frontend embedded in Spring Boot)"
    echo ""
    echo "Default: production"
    exit 1
fi
