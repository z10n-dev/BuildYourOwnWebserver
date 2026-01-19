# Build-Stage (Optional, falls du im Dockerfile bauen willst,
# aber für den Anfang nehmen wir an, du hast ein fertiges JAR oder nutzt Maven/Gradle)
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Kopiere dein kompiliertes Jar (Name anpassen!)
COPY /backend/target/webserver-1.0-SNAPSHOT.jar app.jar

# Copy resources separately from your project
COPY /backend/src/main/resources/static /app/static

# Port freigeben (z.B. 8080)
EXPOSE 8080

# Startbefehl
ENTRYPOINT ["java", "-jar", "app.jar", "/app/static"]