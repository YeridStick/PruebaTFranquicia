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
COPY --from=builder /workspace/applications/app-service/build/libs/*.jar PruebaFranquicia.jar
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
ENV DB_HOST=gondola.proxy.rlwy.net \
    DB_PORT=39683 \
    DB_NAME=railway \
    DB_SCHEMA=public \
    DB_USER=postgres \
    DB_PASSWORD=KlCadXcunKSraYhTlNuzXcWNBOkhscWD

# No especificar EXPOSE cuando se usa $PORT dinámico
# Railway detecta automáticamente el puerto
ENTRYPOINT [ "/bin/sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8082} -jar /app/PruebaFranquicia.jar" ]