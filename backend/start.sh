#!/bin/bash
./mvnw clean package -DskipTests
java -jar target/prototype-0.0.1-SNAPSHOT.jar