FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/mind-file-service-1.0.0-SNAPSHOT.jar mind-file-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "mind-file-service.jar"]
