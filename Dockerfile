# =========================
# 1️⃣ Build stage
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first (better Docker caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source
COPY src ./src

# Build the jar
RUN mvn clean package -DskipTests

# =========================
# 2️⃣ Runtime stage
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/server-0.0.1-SNAPSHOT.jar app.jar

# Expose port (Render uses this)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-jar", "app.jar"]

