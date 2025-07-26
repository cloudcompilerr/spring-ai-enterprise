# Runtime-only Dockerfile - assumes JAR is already built locally
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install necessary packages
RUN apk add --no-cache curl jq

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# Create a non-root user to run the application
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the pre-built JAR file
COPY --chown=spring:spring demo/target/*.jar /app/app.jar

# Create volume for persistent data
VOLUME /app/data

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with proper memory settings
ENTRYPOINT ["java", "-Xmx4g", "-Xms2g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-XX:+UseStringDeduplication", "-XX:MaxMetaspaceSize=512m", "-jar", "/app/app.jar"]