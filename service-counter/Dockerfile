FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY .. /app/.
RUN mvn -f /app/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/service-counter/target/service-counter-*.jar /opt/counter.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/opt/counter.jar"]