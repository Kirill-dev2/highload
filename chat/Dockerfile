FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY .. /app/.
RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/chat/target/chat-*.jar /opt/chat-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/opt/chat-service.jar"]