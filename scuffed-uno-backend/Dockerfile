# ===== Build-Stage =====
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /home/maven/project

# Erst pom.xml kopieren (bessere Layer-Caches), dann src
COPY pom.xml .
COPY src ./src

# Optional: Tests überspringen im CI
RUN mvn clean package -DskipTests

# ===== Runtime-Stage =====
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# JAR aus Build-Stage kopieren (liegt standardmäßig in target/)
COPY --from=build /home/maven/project/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
