FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/mind-gateway-service-1.0.0-SNAPSHOT.jar mind-gateway-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "mind-gateway-service.jar"]
