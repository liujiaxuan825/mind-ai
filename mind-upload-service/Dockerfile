FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/mind-upload-service-1.0.0-SNAPSHOT.jar mind-upload-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "mind-upload-service.jar"]
