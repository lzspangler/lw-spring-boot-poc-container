# ---- Build stage ----
FROM registry.access.redhat.com/ubi9/openjdk-17:1.20 AS build

USER 0

WORKDIR /build

# Copy Maven project files first so dependency resolution can be cached
COPY pom.xml .

# Copy source and other build inputs
COPY src/ src/
COPY openapi/ openapi/

# Build the executable Spring Boot JAR
RUN mvn clean package -DskipTests


# ---- Runtime stage ----
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime:1.20

WORKDIR /deployments

COPY --from=build /build/target/spring-boot-lw-poc-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

USER 185

ENTRYPOINT ["java", "-jar", "/deployments/app.jar"]
