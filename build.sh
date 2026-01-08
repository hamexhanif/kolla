#!/bin/bash
set -e

cd backend
chmod +x ./mvnw
./mvnw clean package -DskipTests

