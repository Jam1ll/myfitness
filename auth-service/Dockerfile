# Stage 1: Build the application using Maven and JDK 25
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies first (leverages Docker layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package the JAR (skipping tests for faster image builds)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight JRE 25 runtime image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy only the compiled JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]