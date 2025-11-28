# -------- Stage 1: Build + Publish common module ----------
FROM gradle:jdk21-corretto-al2023 AS common-builder

# Create working folder for the common module
WORKDIR /common

# Copy the common module
COPY HD-EntityService /common

# Publish the module into Gradle local repository inside Docker
RUN gradle publishToMavenLocal --no-daemon

# ---- Build Stage ----
FROM gradle:jdk21-corretto-al2023 as builder

WORKDIR /app
COPY HD-AuthProject/ .

COPY --from=common-builder /root/.m2 /root/.m2

RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/app.jar"]