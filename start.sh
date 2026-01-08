#!/bin/bash

# Navigate to backend directory
cd backend

# Run the Spring Boot application
# Railway will build the project first, so the JAR should already exist
java -jar target/prototype-0.0.1-SNAPSHOT.jar

