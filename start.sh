#!/usr/bin/env bash

# This script builds the frontend and runs the Spring Boot application with the embedded frontend.
# It is intended for quick start and production-like deployments.

echo "Starting full stack application in production mode..."

# Build the frontend
echo "Building frontend..."
cd frontend
npm install
npm run build
cd ..

# Build and run the Spring Boot application
echo "Building and running Spring Boot application..."
mvn clean install spring-boot:run -Dspring-boot.run.profiles=production
