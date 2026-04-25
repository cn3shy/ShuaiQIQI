FROM docker.1ms.run/library/eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/shuaiqi-backend-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
