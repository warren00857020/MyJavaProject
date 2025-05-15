# ---- 1️⃣ Build stage ----
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -Dmaven.test.skip=true

# ---- 2️⃣ Runtime stage ----
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /build/target/myJavaProject-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -jar app.jar --server.port=${PORT:-8080}"]
