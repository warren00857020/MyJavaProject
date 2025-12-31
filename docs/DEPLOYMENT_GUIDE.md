# Railway 部署指南

本文件說明如何將 MyJavaProject 部署到 Railway 平台。

## 📋 前置需求

- [x] Railway 帳號（[註冊連結](https://railway.app/)）
- [x] GitHub 帳號（用於連接 Railway）
- [x] Google Places API Key
- [x] 已完成本地測試

## 🚀 部署步驟

### 1. 準備 Railway 專案

1. 前往 [Railway](https://railway.app/) 並登入
2. 點擊 **"New Project"**
3. 選擇 **"Deploy from GitHub repo"**
4. 選擇你的 `MyJavaProject` 倉庫

### 2. 設定 PostgreSQL 資料庫

1. 在 Railway 專案中，點擊 **"New Service"**
2. 選擇 **"Database" → "PostgreSQL"**
3. Railway 會自動建立資料庫並提供 `DATABASE_URL`

### 3. 設定環境變數

在 Railway 專案的 **"Variables"** 頁面，新增以下環境變數：

```env
# Google Places API（必填）
GOOGLE_PLACES_API_KEY=你的_Google_API_Key

# PORT 會自動由 Railway 注入，無需手動設定
# DATABASE_URL 會自動由 PostgreSQL 服務注入，無需手動設定
```

### 4. 部署應用程式

1. Railway 會自動偵測 `Dockerfile` 並開始建置
2. 建置完成後會自動部署
3. 點擊 **"Settings" → "Networking" → "Generate Domain"** 取得公開網址

### 5. 驗證部署

訪問以下端點確認部署成功：

```bash
# 健康檢查
curl https://你的應用網址.railway.app/actuator/health

# 查詢景點
curl https://你的應用網址.railway.app/sights

# 查詢美食
curl https://你的應用網址.railway.app/foods

# 查詢節慶
curl https://你的應用網址.railway.app/festivals
```

## 📊 監控與日誌

### 查看日誌

1. 在 Railway 專案中點擊你的服務
2. 選擇 **"Deployments"** 頁籤
3. 點擊最新的部署查看即時日誌

### 監控資源使用

1. 在 **"Metrics"** 頁籤查看：
   - CPU 使用率
   - 記憶體使用率
   - 網路流量

## 🔧 常見問題排解

### 1. 建置失敗

**問題**：Docker 建置超時或失敗

**解決方案**：
- 檢查 `Dockerfile` 是否正確
- 確認 `pom.xml` 依賴項沒有錯誤
- 查看建置日誌找出具體錯誤

### 2. 啟動失敗

**問題**：應用程式無法啟動

**解決方案**：
- 確認環境變數已正確設定（特別是 `GOOGLE_PLACES_API_KEY`）
- 檢查 PostgreSQL 服務是否正常運行
- 查看應用程式日誌找出錯誤訊息

### 3. 資料庫連線失敗

**問題**：無法連接到 PostgreSQL

**解決方案**：
- 確認 `DATABASE_URL` 已自動注入
- 檢查 `application.properties` 中的設定
- 重新部署應用程式

### 4. 記憶體不足

**問題**：應用程式因記憶體不足而崩潰

**解決方案**：
- Railway 免費方案提供 512MB RAM
- 調整 JVM 參數（已在 `Dockerfile` 中設定 `-XX:MaxRAMPercentage=75.0`）
- 考慮升級 Railway 方案

## 💰 費用估算

### Railway 免費方案限制

- ✅ 每月 $5 免費額度（約 500 小時運行時間）
- ✅ 512MB RAM
- ✅ 1GB 磁碟空間
- ✅ 100GB 網路流量

### 預估月費用

對於小規模應用（每日流量 < 1000 requests）：
- **免費方案足夠使用**

對於中規模應用（每日流量 1000-10000 requests）：
- **約 $5-$10/月**（需升級方案）

## 🔄 持續部署 (CI/CD)

Railway 支援自動部署：

1. **自動部署**：每次推送到 GitHub 的 `main` 分支時自動部署
2. **手動部署**：在 Railway 儀表板點擊 **"Redeploy"**

### 設定自動部署分支

1. 前往 **"Settings" → "Service"**
2. 在 **"Source"** 區塊選擇要自動部署的分支（預設為 `main`）

## 📝 環境變數說明

| 變數名稱 | 說明 | 範例 | 必填 |
|---------|------|------|------|
| `DATABASE_URL` | PostgreSQL 連線字串 | `postgresql://user:pass@host:5432/db` | ✅ 自動注入 |
| `GOOGLE_PLACES_API_KEY` | Google Places API 金鑰 | `AIzaSy...` | ✅ 需手動設定 |
| `PORT` | 應用程式埠號 | `8080` | ✅ 自動注入 |

## 🎯 下一步

部署成功後，你可以：

1. ✅ 設定排程任務（使用 Railway Cron Jobs）
2. ✅ 設定自訂網域
3. ✅ 啟用 HTTPS（Railway 自動提供）
4. ✅ 設定監控告警
5. ✅ 串接前端應用程式

## 📚 相關資源

- [Railway 官方文件](https://docs.railway.app/)
- [Spring Boot 部署最佳實踐](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL 官方文件](https://www.postgresql.org/docs/)

---

**部署完成！🎉**

如有任何問題，請參考 [Railway Community](https://discord.gg/railway) 或查閱專案文件。
