# Use Java 17 (Spring Boot compatible)
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy jar file from target folder
COPY target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]