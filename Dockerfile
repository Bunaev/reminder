FROM maven:4.0.0-rc-4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src/
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
COPY --from=builder /app/target/*.jar reminder.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "reminder.jar"]
LABEL authors="bunaev"