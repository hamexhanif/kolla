# Use Eclipse Temurin JDK 21 for building
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app/backend

# Copy pom.xml first for better caching
COPY backend/pom.xml ./

# Copy source code
COPY backend/src ./src

# Build the application using Maven
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/backend/target/prototype-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Railway will set PORT env var)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

