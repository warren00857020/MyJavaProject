#!/bin/bash

# 本地 CI/CD 模擬腳本
set -e  # 遇到錯誤就停止

echo "================================"
echo "本地 CI/CD 模擬開始"
echo "================================"

# 設定變數
PROJECT_NAME="spring"
DOCKER_IMAGE_NAME="myapp"
DOCKER_TAG="local-test"

# Step 1: 清理舊的 build
echo "📦 Step 1: 清理舊的 build..."
./mvnw clean || ./gradlew clean

# Step 2: 運行測試
echo "🧪 Step 2: 運行測試..."
./mvnw test || ./gradlew test

# Step 3: 建置專案
echo "🔨 Step 3: 建置專案..."
./mvnw package -DskipTests || ./gradlew build -x test

# Step 4: 建置 Docker Image
echo "🐳 Step 4: 建置 Docker Image..."
if [ -f "Dockerfile" ]; then
    docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_TAG} .
    echo "✅ Docker Image 建置成功: ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}"
else
    echo "❌ 找不到 Dockerfile"
    exit 1
fi

# Step 5: 列出建置的 Image
echo "📋 Step 5: 已建置的 Images:"
docker images | grep ${DOCKER_IMAGE_NAME}

# Step 6 (Optional): 運行容器測試
echo "🚀 Step 6: 要啟動容器進行測試嗎？(y/n)"
read -r response
if [[ "$response" == "y" ]]; then
    echo "啟動容器..."
    docker run -d -p 8080:8080 --name ${DOCKER_IMAGE_NAME}-test ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}
    echo "✅ 容器已啟動在 http://localhost:8080"
    echo "停止容器請執行: docker stop ${DOCKER_IMAGE_NAME}-test && docker rm ${DOCKER_IMAGE_NAME}-test"
fi

echo "================================"
echo "✅ 本地 CI/CD 完成！"
echo "================================"
