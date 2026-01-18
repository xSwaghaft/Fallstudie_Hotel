# Stage 1: build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
# Copy frontend directory (contains themes and frontend resources)
COPY frontend ./frontend
# Copy frontend configuration files
COPY package.json package-lock.json* ./
COPY tsconfig.json* types.d.ts* vite.config.ts* vite.generated.ts* ./
RUN mvn -B -DskipTests -Dvaadin.productionMode=true -Pproduction package

# Stage 2: runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copy jar produced by the build stage. Adjust name if your artifactId/version differ.
COPY --from=build /workspace/target/*.jar ./app.jar
RUN mkdir -p /app/data/images
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
