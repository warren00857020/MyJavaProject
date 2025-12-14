# ---- 1️⃣ Build stage ----
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /build

# 先複製 pom.xml 並下載依賴（利用 Docker 快取層）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 複製原始碼並編譯
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- 2️⃣ Runtime stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 建立非 root 使用者提升安全性
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 從建置階段複製 JAR 檔
COPY --from=build /build/target/myJavaProject-*.jar app.jar

# 暴露埠號
EXPOSE 8080

# 啟動應用程式（優化 JVM 參數）
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", "app.jar"]
