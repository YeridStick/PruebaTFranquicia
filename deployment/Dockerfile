FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /workspace
COPY gradlew .
COPY gradle gradle
COPY settings.gradle build.gradle main.gradle gradle.properties ./
COPY applications applications
COPY domain domain
COPY infrastructure infrastructure
COPY deployment deployment
COPY lombok.config .
RUN chmod +x gradlew && ./gradlew clean build --no-daemon

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=builder /workspace/applications/app-service/build/libs/*.jar PruebaFranquicia.jar
# Removed OpenJ9-specific -Xshareclasses; Temurin (HotSpot) doesn't support it.
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
# Default DB connection (can be overridden at runtime)
ENV DB_HOST=gondola.proxy.rlwy.net \
    DB_PORT=39683 \
    DB_NAME=railway \
    DB_SCHEMA=public \
    DB_USER=postgres \
    DB_PASSWORD=KlCadXcunKSraYhTlNuzXcWNBOkhscWD
EXPOSE 8082
ENTRYPOINT [ "/bin/sh", "-c", "java $JAVA_OPTS -jar /app/PruebaFranquicia.jar" ]
