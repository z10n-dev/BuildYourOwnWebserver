# Build-Stage (Optional, falls du im Dockerfile bauen willst,
# aber für den Anfang nehmen wir an, du hast ein fertiges JAR oder nutzt Maven/Gradle)
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Kopiere dein kompiliertes Jar (Name anpassen!)
COPY /backend/target/webserver-1.0-SNAPSHOT.jar app.jar

# Copy resources to a temporary location
COPY /backend/src/main/resources/static /app/www-default
COPY /backend/src/main/resources/config /app/config-default

# Create empty directories for volumes
RUN mkdir -p /app/www /app/config

# Port freigeben (z.B. 8080)
EXPOSE 8080

# Create entrypoint script that copies defaults if directories are empty
RUN echo '#!/bin/sh' > /entrypoint.sh && \
    echo 'if [ -z "$(ls -A /app/config)" ]; then' >> /entrypoint.sh && \
    echo '  echo "Config directory empty, copying defaults..."' >> /entrypoint.sh && \
    echo '  cp -r /app/config-default/* /app/config/' >> /entrypoint.sh && \
    echo 'fi' >> /entrypoint.sh && \
    echo 'if [ -z "$(ls -A /app/www)" ]; then' >> /entrypoint.sh && \
    echo '  echo "WWW directory empty, copying defaults..."' >> /entrypoint.sh && \
    echo '  cp -r /app/www-default/* /app/www/' >> /entrypoint.sh && \
    echo 'fi' >> /entrypoint.sh && \
    echo 'exec java -jar app.jar prod' >> /entrypoint.sh && \
    chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
