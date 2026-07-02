FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/mind-ai-service-1.0.0-SNAPSHOT.jar mind-ai-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "mind-ai-service.jar"]
