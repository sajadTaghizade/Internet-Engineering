# --- Stage 1: build the Spring Boot fat jar with Maven + a full JDK ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only the POM first so Maven's dependency cache is a separate Docker layer
# and doesn't get invalidated every time application source code changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# --- Stage 2: run the jar on a minimal JRE-only image ---
# We only need a JRE (not a full JDK) to *run* an already-built jar, and Alpine
# keeps the final image small since none of the Maven/JDK build tooling ships
# in the runtime image.
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /build/target/dynamiccontentserver-*.jar app.jar
USER app

EXPOSE 9092
ENTRYPOINT ["java", "-jar", "app.jar"]
