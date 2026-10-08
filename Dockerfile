# ==============================================================================
# MYMART — Multi-Stage Production Dockerfile (Java 21 + Spring Boot 3.3)
# ==============================================================================

# Stage 1: Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy Maven descriptor and download dependencies (cached layer)
COPY backend/pom.xml ./backend/
RUN cd backend && mvn dependency:go-offline -B || true

# Copy frontend static files and backend source code
COPY frontend ./frontend
COPY backend ./backend

# Sync latest frontend files into backend static resources
RUN cp -r ./frontend/* ./backend/src/main/resources/static/

# Build and package production archive
RUN cd backend && mvn clean package -DskipTests

# Stage 2: Minimalist High-Performance Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add unprivileged user for production security
RUN addgroup -S mymart && adduser -S mymart -G mymart

# Copy packaged JAR from builder
COPY --from=builder /workspace/backend/target/mymart-backend-1.0.0.jar /app/mymart.jar
COPY --from=builder /workspace/frontend /app/frontend

RUN chown -R mymart:mymart /app
USER mymart

# Default environment configuration
ENV PORT=8080
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/mymart.jar"]
