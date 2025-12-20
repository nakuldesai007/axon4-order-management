#!/bin/bash
set -e

# Install Maven via apt-get (Debian/Ubuntu package manager)
echo "Installing Maven..."
sudo apt-get update
sudo apt-get install -y maven

# Verify installation
mvn --version

echo "Maven installation completed successfully!"


