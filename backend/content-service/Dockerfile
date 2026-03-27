# Content Service Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY content-service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
