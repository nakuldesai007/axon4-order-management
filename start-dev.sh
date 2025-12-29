#!/usr/bin/env bash

# This script runs the Spring Boot application in development mode and
# starts the frontend development server with hot reload.

echo "Starting full stack application in development mode..."

# Start Spring Boot backend in a separate process
echo "Starting Spring Boot backend..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev &

# Wait for the backend to start up (optional, but good practice)
echo "Waiting for backend to start (approx 20 seconds)..."
sleep 20

# Start frontend development server
echo "Starting frontend development server..."
cd frontend
npm install
npm run dev
cd ..