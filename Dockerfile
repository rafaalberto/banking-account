FROM eclipse-temurin:21-jre-jammy

# Create a non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy JAR file
ARG JAR_FILE=build/libs/banking-account.jar
COPY ${JAR_FILE} app.jar

# Create data directory for H2 database and set ownership
RUN mkdir -p /app/data && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV H2_DATABASE_PATH="/app/data/test"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
