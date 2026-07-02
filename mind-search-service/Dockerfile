FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/mind-search-service-1.0.0-SNAPSHOT.jar mind-search-service.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "mind-search-service.jar"]
