# User Service Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY user-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
