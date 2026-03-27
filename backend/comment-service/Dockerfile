# Comment Service Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY comment-service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
