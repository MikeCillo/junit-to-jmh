
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY api api
COPY converter converter
RUN ./gradlew :converter:installDist --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/converter/build/install/converter /app/
# entrypoint
ENTRYPOINT ["/app/bin/converter"]