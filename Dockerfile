# ===============================
# Build Stage
# ===============================
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

# Copy source
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests

# ===============================
# Runtime Stage
# ===============================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy jar
COPY --from=build /app/target/*.jar app.jar

# Switch to non-root
USER spring

# Default port
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -jar app.jar --server.port=${PORT}"]