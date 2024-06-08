FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY .. /app/.
RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/core/target/core-*.jar /opt/highload-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/highload-service.jar"]