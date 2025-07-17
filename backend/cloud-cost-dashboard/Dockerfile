# Use an official JDK base image
FROM openjdk:17-jdk-slim

# Set environment variables
ENV APP_HOME=/app
WORKDIR $APP_HOME

# Copy built jar file (assumes it's named target/cloud-cost-dashboard.jar)
COPY target/cloud-cost-dashboard.jar app.jar

# Expose application port
EXPOSE 8080

# Default command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
