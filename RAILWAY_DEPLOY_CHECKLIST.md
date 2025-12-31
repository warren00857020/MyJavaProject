# 🚀 Railway 快速部署清單

## ✅ 部署前確認

- [x] 所有修改已 commit
- [x] 環境變數配置完成（.env.example）
- [x] Docker 配置完成（Dockerfile + .dockerignore）
- [x] Railway 配置完成（railway.json）
- [x] 小規模測試完成（20 Sights + 20 Foods + 20 Festivals）
- [x] MCP Server 測試完成

## 📝 部署步驟（約 10 分鐘）

### 1. 推送程式碼到 GitHub（2 分鐘）

```bash
# 確認當前分支
git branch

# 推送到遠端
git push origin claude/review-github-actions-ci-PXjZH

# 或合併到 main 分支後推送
git checkout main
git merge claude/review-github-actions-ci-PXjZH
git push origin main
```

### 2. 登入 Railway 並建立專案（2 分鐘）

1. 前往 https://railway.app/
2. 使用 GitHub 帳號登入
3. 點擊 **"New Project"**
4. 選擇 **"Deploy from GitHub repo"**
5. 選擇 `MyJavaProject` 倉庫
6. 選擇 `main` 分支（或 `claude/review-github-actions-ci-PXjZH`）

### 3. 新增 PostgreSQL 資料庫（1 分鐘）

1. 在 Railway 專案中，點擊 **"New Service"**
2. 選擇 **"Database" → "PostgreSQL"**
3. Railway 會自動建立並注入 `DATABASE_URL` 環境變數

### 4. 設定環境變數（1 分鐘）

在 Railway 專案的 **"Variables"** 頁面新增：

```env
GOOGLE_PLACES_API_KEY=AIzaSyCJTzMWkfxhnUx3haP0qjULGbTVAm5GDuQ
```

> 注意：`DATABASE_URL` 和 `PORT` 會自動由 Railway 注入，無需手動設定

### 5. 觸發部署（3 分鐘）

1. Railway 會自動偵測 `Dockerfile` 並開始建置
2. 建置時間約 2-3 分鐘
3. 建置完成後自動部署

### 6. 取得公開網址（1 分鐘）

1. 點擊你的服務（myJavaProject）
2. 前往 **"Settings" → "Networking"**
3. 點擊 **"Generate Domain"**
4. 複製生成的網址（例：`your-app.railway.app`）

## 🧪 驗證部署

部署完成後，測試以下端點：

```bash
# 替換為你的實際網址
export RAILWAY_URL="https://your-app.railway.app"

# 1. 健康檢查（應返回 200 OK）
curl $RAILWAY_URL/actuator/health

# 2. 查詢景點（應返回 JSON 陣列）
curl $RAILWAY_URL/sights

# 3. 查詢美食（應返回 JSON 陣列）
curl $RAILWAY_URL/foods

# 4. 查詢節慶（應返回 JSON 陣列）
curl $RAILWAY_URL/festivals

# 5. 測試 Google Places API（應返回成功訊息）
curl "$RAILWAY_URL/foods/test-api?lat=25.033&lng=121.565&radius=500&maxResults=5"
```

## 📊 監控與管理

### 查看日誌

1. 在 Railway 專案中點擊你的服務
2. 選擇 **"Deployments"** 頁籤
3. 點擊最新的部署查看即時日誌

### 資源監控

前往 **"Metrics"** 頁籤查看：
- CPU 使用率
- 記憶體使用率（應 < 512MB）
- 網路流量

## ⚠️ 常見問題

### 問題 1：建置失敗

**檢查**：
- Dockerfile 是否正確
- pom.xml 依賴是否完整
- 建置日誌中的具體錯誤

**解決**：
```bash
# 本地測試 Docker 建置
docker build -t myapp .
docker run -p 8080:8080 myapp
```

### 問題 2：啟動失敗

**檢查**：
- `GOOGLE_PLACES_API_KEY` 是否已設定
- PostgreSQL 服務是否正常
- 應用程式日誌中的錯誤訊息

**解決**：
前往 Railway → Variables → 確認環境變數無誤

### 問題 3：資料庫連線失敗

**檢查**：
- PostgreSQL 服務狀態
- `DATABASE_URL` 是否自動注入

**解決**：
在 Railway 日誌中搜尋 `DATABASE_URL` 確認值是否正確

## 🎯 部署後任務

部署成功後，你可以：

1. **設定自訂網域**（可選）
   - Settings → Networking → Custom Domain

2. **啟用自動部署**（已預設啟用）
   - 每次推送到 GitHub 會自動部署

3. **設定告警通知**（可選）
   - Settings → Notifications

4. **測試 MCP Server 整合**
   - 將 Railway URL 更新到 MCP Server 配置
   - 測試 Claude Desktop 整合

5. **開始爬取完整資料**
   - 使用 Railway URL 調用爬蟲 API
   - 監控資源使用狀況

## 💡 下一步建議

### 立即可做
- ✅ 測試所有 API 端點
- ✅ 確認資料庫連線正常
- ✅ 驗證 Google Places API 運作

### 近期規劃
- 🔄 設定排程任務（每週自動爬取）
- 📱 建立前端應用程式
- 🔍 優化資料庫查詢效能
- 📊 新增監控儀表板

### 長期規劃
- 🌐 支援更多縣市
- 🔐 新增使用者認證
- 📈 建立分析報表功能
- 🚀 擴展到更多旅遊資料來源

---

**準備好了嗎？開始部署吧！** 🚀

有任何問題請參考 [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md)
