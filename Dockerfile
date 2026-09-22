# =========================================================================
# Stage 1: Build Jar in Maven environment
# =========================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and download dependencies to utilize Docker cache layers
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source tree and build jar
COPY src ./src
RUN mvn package -DskipTests -B

# =========================================================================
# Stage 2: Minimal Java Runtime Environment
# =========================================================================
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Run as non-privileged system user for high-security isolation
RUN addgroup -S mfgops && adduser -S mfgops -G mfgops
USER mfgops

# Copy only the compiled jar from builder stage
COPY --from=builder /app/target/fabricore-0.0.1-SNAPSHOT.jar ./fabricore.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "fabricore.jar"]
