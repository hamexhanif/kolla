#!/bin/bash
set -e

# Ensure we're in the backend directory (rootDirectory should handle this, but just in case)
if [ -d "backend" ]; then
  cd backend
fi

# Make mvnw executable and run it
chmod +x ./mvnw || true
bash ./mvnw clean package -DskipTests

