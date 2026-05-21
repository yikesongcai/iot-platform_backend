# ---- 第一阶段：Maven 编译 ----
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build-stage
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# ---- 第二阶段：JRE 运行 ----
FROM eclipse-temurin:17-jre-alpine AS production-stage
WORKDIR /app
COPY --from=build-stage /app/target/*.jar app.jar
VOLUME /app/logs

EXPOSE 8084
EXPOSE 1884
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
