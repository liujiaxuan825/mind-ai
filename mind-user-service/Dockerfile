FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/mind-user-service-1.0.0-SNAPSHOT.jar mind-user-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "mind-user-service.jar"]
