# 使用輕量版的 JDK 基底映像
FROM eclipse-temurin:17-jdk-alpine

# 建立 app 目錄
WORKDIR /app

# 複製剛剛打包好的 jar 到容器
COPY target/myJavaProject-0.0.1-SNAPSHOT.jar App.jar

# 開放 Spring Boot 預設 port（可調整）
EXPOSE 8080

# 執行 Spring Boot 程式
ENTRYPOINT ["java", "-jar", "App.jar"]