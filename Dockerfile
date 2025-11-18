# Stage 1: build
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests -Dvaadin.productionMode=true -Pproduction package

# Stage 2: runtime
FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
# Copy jar produced by the build stage. Adjust name if your artifactId/version differ.
COPY --from=build /workspace/target/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
