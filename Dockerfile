# Stage 1: build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
# Copy frontend files if they exist
COPY frontend ./frontend
COPY package.json ./
COPY vite.config.ts ./
COPY vite.generated.ts ./
COPY tsconfig.json ./
# Build with production mode - this will build the frontend bundle
RUN mvn -B -DskipTests -Dvaadin.productionMode=true -Pproduction clean package

# Stage 2: runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy jar produced by the build stage. Adjust name if your artifactId/version differ.
# The data.sql file is already included in the JAR (from src/main/resources/data.sql)
COPY --from=build /workspace/target/*.jar ./app.jar

# Create data/images directory with proper permissions
RUN mkdir -p /app/data/images && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

EXPOSE 8080

# Use production profile by default in Docker
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=production"]
