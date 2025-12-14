# 本地 CI/CD 模擬指南

這份文件說明如何在本地電腦上模擬 GitHub Actions CI/CD 環境。

## 📋 目錄

1. [方案一：使用 act 工具](#方案一使用-act-工具)
2. [方案二：使用本地腳本](#方案二使用本地腳本)
3. [方案三：使用 Docker Compose](#方案三使用-docker-compose)

---

## 方案一：使用 act 工具

### 安裝 act

```bash
# macOS
brew install act

# Linux
curl https://raw.githubusercontent.com/nektos/act/master/install.sh | sudo bash

# Windows (Chocolatey)
choco install act-cli
```

### 設定 Secrets

建立 `.secrets` 檔案（不要提交到 git）：

```bash
DOCKER_USERNAME=your_username
DOCKER_PASSWORD=your_password
```

將 `.secrets` 加入 `.gitignore`：

```bash
echo ".secrets" >> .gitignore
```

### 運行 Workflow

```bash
# 列出所有 workflows
act -l

# 模擬 push 事件（本地測試，不會真的推送）
act push --secret-file .secrets

# 運行特定的 job
act -j build --secret-file .secrets

# Dry run（只顯示會執行什麼，不真的執行）
act -n

# 使用特定的 Docker image
act -P ubuntu-latest=catthehacker/ubuntu:act-latest
```

### act 的限制

- ⚠️ 不支援某些 GitHub 專屬功能（如 OIDC）
- ⚠️ 需要 Docker 環境
- ⚠️ 第一次執行會下載較大的 Docker image

---

## 方案二：使用本地腳本

### 使用方式

```bash
# 1. 載入環境變數
source .env.local

# 2. 執行本地 CI 腳本
./local-ci.sh
```

### 腳本內容

`local-ci.sh` 會執行：

1. ✅ 清理舊的 build
2. ✅ 運行測試
3. ✅ 建置專案
4. ✅ 建置 Docker Image
5. ✅ (選擇性) 啟動容器測試

### 自訂腳本

你可以修改 `local-ci.sh` 來符合你的需求：

```bash
# 例如：加入程式碼檢查
echo "🔍 運行程式碼檢查..."
./mvnw checkstyle:check

# 例如：加入安全掃描
echo "🔒 掃描安全漏洞..."
docker scan ${DOCKER_IMAGE_NAME}:${DOCKER_TAG}
```

---

## 方案三：使用 Docker Compose

### 快速啟動

```bash
# 建置並啟動所有服務
docker-compose -f docker-compose.local.yml up --build

# 背景執行
docker-compose -f docker-compose.local.yml up -d

# 查看 logs
docker-compose -f docker-compose.local.yml logs -f

# 停止服務
docker-compose -f docker-compose.local.yml down
```

### 搭配資料庫測試

如果需要資料庫，編輯 `docker-compose.local.yml` 並取消註解資料庫相關的部分。

---

## 🔧 進階：模擬 GCP Cloud Run 部署

### 本地運行 Cloud Run 容器

```bash
# 1. 建置 image
docker build -t myapp:local .

# 2. 使用 Cloud Run 環境變數運行
docker run -p 8080:8080 \
  -e PORT=8080 \
  -e K_SERVICE=myapp \
  -e K_REVISION=myapp-00001-local \
  myapp:local
```

### 使用 Cloud Run Emulator

```bash
# 安裝 gcloud
# https://cloud.google.com/sdk/docs/install

# 使用本地容器模擬 Cloud Run
gcloud run services replace service.yaml --region=asia-east1
```

---

## 📝 GitHub Actions 修改建議

### 1. 加入本地測試模式

修改 workflow 加入條件判斷：

```yaml
- name: Build Docker image
  if: github.event_name == 'workflow_dispatch'  # 手動觸發時不推送
  uses: docker/build-push-action@v5
  with:
    push: false  # 本地測試時不推送
```

### 2. 使用環境變數

讓 workflow 更容易在不同環境運行：

```yaml
env:
  DOCKER_REGISTRY: ${{ vars.DOCKER_REGISTRY || 'docker.io' }}
  IMAGE_NAME: ${{ vars.IMAGE_NAME || 'myapp' }}
```

### 3. 拆分 Job

將 build、test、deploy 拆成獨立的 job：

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps: [...]

  build:
    needs: test
    runs-on: ubuntu-latest
    steps: [...]

  deploy:
    needs: build
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps: [...]
```

---

## 🎯 最佳實踐

### 1. 環境隔離

```bash
# 使用不同的 tag 區分環境
local-test        # 本地開發
dev-*             # 開發環境
staging-*         # 測試環境
prod-*            # 正式環境
```

### 2. 快取機制

```bash
# Docker layer caching
docker build --cache-from myapp:latest -t myapp:new .

# Maven/Gradle 快取
./mvnw -Dmaven.repo.local=.m2/repository package
```

### 3. 平行測試

```bash
# 同時運行多個測試
./mvnw test -T 4  # 使用 4 個執行緒
```

---

## 🐛 常見問題

### Q: act 執行時出現權限錯誤？

```bash
# 確保 Docker daemon 正在運行
sudo systemctl start docker

# 將使用者加入 docker 群組
sudo usermod -aG docker $USER
```

### Q: 本地容器無法連接資料庫？

```bash
# 使用 docker network
docker network create app-network
docker run --network app-network ...
```

### Q: 如何清理 Docker 資源？

```bash
# 清理未使用的 image
docker image prune -a

# 清理所有停止的容器
docker container prune

# 清理所有未使用的資源
docker system prune -a
```

---

## 📚 參考資源

- [act - GitHub Actions 本地執行工具](https://github.com/nektos/act)
- [Docker Compose 文件](https://docs.docker.com/compose/)
- [Cloud Run 本地開發](https://cloud.google.com/run/docs/testing/local)
