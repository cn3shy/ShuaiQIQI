# Notification Service Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY notification-service/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
