#!/bin/bash
set -e

echo "🚀 Starting Axon Order Management System in DEVELOPMENT mode..."
echo ""
echo "This will start:"
echo "  - Backend on http://localhost:8080"
echo "  - Frontend on http://localhost:3000 (with hot reload)"
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

# Start backend
echo "📦 Starting backend..."
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
echo "📦 Starting frontend..."
cd frontend

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "   Installing frontend dependencies..."
    npm install
fi

npm run dev &
FRONTEND_PID=$!

echo ""
echo "✅ Both servers are running!"
echo "   Frontend: http://localhost:3000/"
echo "   Backend API: http://localhost:8080/api/orders"
echo "   Swagger: http://localhost:8080/swagger-ui.html"
echo "   H2 Console: http://localhost:8080/h2-console"
echo ""

# Wait for both processes
wait

