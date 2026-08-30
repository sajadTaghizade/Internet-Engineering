# --- Stage 1: build the Spring Boot fat jar with Maven + a full JDK ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Optional Maven mirror. Maven Central (repo.maven.apache.org) is slow/unreachable
# from some networks/ISPs, which makes `dependency:go-offline` hang or fail with a
# truncated download. Left empty by default (Maven Central is used directly); set
# MAVEN_MIRROR_URL in your .env (see .env.example) to route dependency downloads
# through a mirror instead, e.g. https://maven.aliyun.com/repository/public.
ARG MAVEN_MIRROR_URL=
RUN if [ -n "$MAVEN_MIRROR_URL" ]; then \
      mkdir -p /root/.m2 && \
      printf '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">\n  <mirrors>\n    <mirror>\n      <id>custom-mirror</id>\n      <mirrorOf>central</mirrorOf>\n      <url>%s</url>\n    </mirror>\n  </mirrors>\n</settings>\n' "$MAVEN_MIRROR_URL" > /root/.m2/settings.xml; \
    fi

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
