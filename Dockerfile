# Multi-stage build for Spring AI Enterprise

# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy the Maven POM files first to leverage Docker cache
COPY pom.xml .
COPY api/pom.xml api/
COPY core/pom.xml core/
COPY demo/pom.xml demo/
COPY mcp/pom.xml mcp/
COPY prompt-engineering/pom.xml prompt-engineering/
COPY rag/pom.xml rag/
COPY service/pom.xml service/
COPY vector-db/pom.xml vector-db/

# Download dependencies (this layer will be cached if pom files don't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY api/src api/src
COPY core/src core/src
COPY demo/src demo/src
COPY mcp/src mcp/src
COPY prompt-engineering/src prompt-engineering/src
COPY rag/src rag/src
COPY service/src service/src
COPY vector-db/src vector-db/src
COPY docs docs

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
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

# Copy the built artifacts from the build stage
COPY --from=build --chown=spring:spring /app/demo/target/*.jar /app/app.jar

# Create volume for persistent data
VOLUME /app/data

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Default command line arguments
CMD ["--spring.profiles.active=prod"]