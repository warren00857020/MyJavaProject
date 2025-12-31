# MCP Server 測試計畫

**目標**：驗證 Claude Code 能夠成功呼叫你的 Taiwan Travel MCP Server

---

## 📋 測試前準備

### 1. 啟動 PostgreSQL

```powershell
# 檢查 PostgreSQL 是否運行
docker ps

# 如果沒有運行，啟動它
docker start <container_id>
```

### 2. 啟動 Spring Boot

```powershell
cd C:\Users\warre\OneDrive\文件\MyJavaProject
./mvnw.cmd spring-boot:run
```

**等待看到**：
```
Started MyJavaProjectApplication in X.XXX seconds
```

### 3. 驗證 API 可訪問

在瀏覽器開啟或用 curl 測試：
```
http://localhost:8080/sights
http://localhost:8080/foods
http://localhost:8080/festivals/ongoing
```

應該回傳 JSON 資料（即使是空陣列也沒關係）。

---

## 🧪 測試案例

### 測試 1: 搜尋景點（基礎）

**在新的 Claude Code 對話中輸入**：
```
幫我找士林區的景點
```

**預期行為**：
1. Claude 應該會使用 `search_sights` tool
2. 呼叫 `http://localhost:8080/sights?zone=士林區`
3. 回傳資料庫中士林區的景點

**成功指標**：
- ✅ Claude 回覆中包含景點名稱、地址等真實資料
- ✅ 不是網路搜尋結果
- ✅ 資料來自你的資料庫

**失敗指標**：
- ❌ Claude 使用 WebSearch 而非 Tool
- ❌ 回覆「我沒有相關工具」
- ❌ 錯誤訊息

---

### 測試 2: 搜尋美食（關鍵字）

**輸入**：
```
推薦信義區的拉麵店
```

**預期**：
- 使用 `search_foods` tool
- 參數：`zone=信義區, keyword=拉麵`
- 回傳餐廳資料（名稱、評分、地址）

---

### 測試 3: 搜尋活動（狀態篩選）

**輸入**：
```
有什麼節慶活動正在進行？
```

**預期**：
- 使用 `search_festivals` tool
- 參數：`status=ongoing`
- 回傳進行中的活動

---

### 測試 4: 取得詳細資訊

**輸入**：
```
告訴我 ID 為 1 的景點詳細資訊
```

**預期**：
- 使用 `get_sight_details` tool
- 參數：`sightId=1`
- 回傳完整的景點資料

---

### 測試 5: 複雜查詢（多參數）

**輸入**：
```
幫我找士林區的博物館，最多顯示 5 個
```

**預期**：
- 使用 `search_sights` tool
- 參數：`zone=士林區, category=博物館, limit=5`

---

## 🐛 問題診斷

### 問題 1: Claude 沒有使用 Tools

**症狀**：
- Claude 使用 WebSearch 或說「我沒有這個功能」

**檢查**：
1. 確認 `.mcp.json` 存在且格式正確
2. 確認 `settings.local.json` 有 `enabledMcpjsonServers: ["taiwan-travel"]`
3. 確認 `mcp-server/dist/index.js` 存在
4. **重要**：必須在**新的對話**中測試（舊對話不會載入 MCP Server）

**解決**：
```powershell
# 重新編譯 MCP Server
cd mcp-server
npm run build

# 確認檔案存在
ls dist/index.js
```

---

### 問題 2: MCP Server 啟動失敗

**症狀**：
- Claude 嘗試使用 Tool 但失敗
- 錯誤訊息提到 MCP Server

**檢查**：
```powershell
# 手動測試 MCP Server 是否能啟動
cd C:\Users\warre\OneDrive\文件\MyJavaProject
node mcp-server/dist/index.js
```

**預期輸出**：
```
🚀 Taiwan Travel MCP Server 已啟動
📡 連接到 Java API: http://localhost:8080
✅ 可用的 Tools:
  - search_sights: 搜尋景點
  - search_foods: 搜尋美食
  - search_festivals: 搜尋節慶活動
  - get_sight_details: 取得景點詳細資訊
  - get_food_details: 取得餐廳詳細資訊
```

如果啟動失敗，查看錯誤訊息。

---

### 問題 3: API 連接失敗

**症狀**：
- MCP Server 啟動成功
- Claude 呼叫 Tool 成功
- 但回傳錯誤或空資料

**檢查**：
```powershell
# 測試 Spring Boot API
curl http://localhost:8080/sights
```

**如果失敗**：
- Spring Boot 沒有運行 → 啟動它
- Port 8080 被佔用 → 更改 Port 或關閉佔用的程式
- 資料庫連接失敗 → 檢查 PostgreSQL

---

## 📊 測試結果記錄

### 測試 1: 搜尋景點
- [ ] 已測試
- [ ] 成功 / 失敗
- 備註：_________________

### 測試 2: 搜尋美食
- [ ] 已測試
- [ ] 成功 / 失敗
- 備註：_________________

### 測試 3: 搜尋活動
- [ ] 已測試
- [ ] 成功 / 失敗
- 備註：_________________

### 測試 4: 取得詳細資訊
- [ ] 已測試
- [ ] 成功 / 失敗
- 備註：_________________

### 測試 5: 複雜查詢
- [ ] 已測試
- [ ] 成功 / 失敗
- 備註：_________________

---

## 🎯 測試通過標準

所有測試都應該：
1. ✅ Claude 正確識別意圖並使用對應的 Tool
2. ✅ Tool 成功呼叫 Spring Boot API
3. ✅ 回傳真實的資料庫資料
4. ✅ Claude 用自然語言正確呈現結果

---

## 📝 下一步（測試成功後）

1. **補充資料**
   - 執行完整的景點爬蟲
   - 執行完整的美食爬蟲
   - 執行完整的活動爬蟲

2. **優化回應**
   - 調整 Tool 的描述，讓 Claude 更容易理解
   - 優化回傳資料的格式

3. **新增進階功能**
   - 實作 `plan_itinerary` tool（智慧行程規劃）
   - 整合 Google Maps API（路線計算）

4. **Claude Desktop 整合**
   - 解決 Claude Desktop 連接問題
   - 測試相同的案例

---

## 🔧 快速啟動腳本（Windows）

建立 `start-dev.bat`：

```batch
@echo off
echo 🚀 啟動 Taiwan Travel AI Assistant 開發環境

echo.
echo [1/3] 檢查 PostgreSQL...
docker ps | findstr postgres
if errorlevel 1 (
    echo ❌ PostgreSQL 未運行，請先啟動 Docker 容器
    pause
    exit
)

echo ✅ PostgreSQL 運行中

echo.
echo [2/3] 啟動 Spring Boot...
cd /d C:\Users\warre\OneDrive\文件\MyJavaProject
start cmd /k "mvnw.cmd spring-boot:run"

echo.
echo [3/3] 等待 Spring Boot 啟動（約 10-20 秒）...
timeout /t 15

echo.
echo ✅ 所有服務已啟動！
echo.
echo 📝 測試 API:
echo    http://localhost:8080/sights
echo    http://localhost:8080/foods
echo    http://localhost:8080/festivals/ongoing
echo.
echo 🎯 現在可以在 Claude Code 中開啟新對話進行測試！
echo.
pause
```

執行此腳本來快速啟動所有服務。

---

**準備好開始測試了嗎？**

1. 啟動 PostgreSQL 和 Spring Boot
2. 在 Claude Code 開啟**新的對話**
3. 嘗試測試案例 1：「幫我找士林區的景點」
4. 告訴我結果！
