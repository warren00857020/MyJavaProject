# MCP Server 整合除錯指南

## 問題：Claude Desktop 沒有使用我的 Tools

### 檢查清單

#### 1. 確認 Spring Boot API 正在運行

```powershell
# 測試 API 是否可訪問
curl http://localhost:8080/sights

# 或在瀏覽器開啟
# http://localhost:8080/sights
```

**預期結果**：應該回傳景點資料的 JSON

**如果失敗**：
```powershell
# 啟動 Spring Boot
cd C:\Users\warre\OneDrive\文件\MyJavaProject
./mvnw.cmd spring-boot:run
```

---

#### 2. 測試 MCP Server 是否能獨立運行

```powershell
cd C:\Users\warre\OneDrive\文件\MyJavaProject\mcp-server

# 測試執行
node dist/index.js
```

**預期結果**：應該看到啟動訊息
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

**如果失敗**：檢查錯誤訊息並解決

---

#### 3. 確認 Claude Desktop 設定

**位置**：`C:\Users\warre\AppData\Roaming\.claude\mcp.json`

**內容**：
```json
{
  "mcpServers": {
    "taiwan-travel": {
      "command": "node",
      "args": ["C:\\Users\\warre\\OneDrive\\文件\\MyJavaProject\\mcp-server\\dist\\index.js"],
      "env": {
        "JAVA_API_URL": "http://localhost:8080"
      }
    }
  }
}
```

**重要**：
- ✅ 路徑使用 `\\` (雙反斜線)
- ✅ 路徑必須是絕對路徑
- ✅ 檔案必須存在：`dist/index.js`

---

#### 4. 完全重啟 Claude Desktop

**重要**：必須完全關閉，不只是關閉視窗！

**Windows**：
1. 關閉所有 Claude Desktop 視窗
2. 開啟工作管理員 (Ctrl+Shift+Esc)
3. 查看「處理程序」分頁
4. 找到「Claude Desktop」或「claude.exe」
5. 右鍵 → 結束工作
6. 重新開啟 Claude Desktop

---

#### 5. 檢查 Claude Desktop 是否識別 MCP Server

**方法 1：查看 Claude Desktop UI**
- 開啟 Claude Desktop
- 查看是否有顯示「taiwan-travel」或工具圖示
- 某些版本會在對話框旁邊顯示可用的 Tools

**方法 2：直接詢問**
在 Claude Desktop 中輸入：
```
你現在有哪些可用的 tools？
```

**預期回應**：應該列出 5 個 Taiwan Travel Tools

---

## 常見問題

### Q1: Claude 說找不到 Tools
**可能原因**：
- mcp.json 格式錯誤（檢查 JSON 語法）
- 路徑不正確
- Claude Desktop 沒有完全重啟

**解決方案**：
1. 用 JSON 驗證器檢查 mcp.json
2. 確認路徑使用 `\\` 而非 `\`
3. 完全重啟 Claude Desktop（參考步驟 4）

---

### Q2: MCP Server 無法連接 Java API
**錯誤訊息**：`ECONNREFUSED` 或 `connect ETIMEDOUT`

**解決方案**：
```powershell
# 1. 確認 Spring Boot 運行中
curl http://localhost:8080/sights

# 2. 檢查防火牆設定
# Windows Defender 防火牆 → 允許應用程式通過防火牆

# 3. 確認 PostgreSQL 運行中
docker ps
```

---

### Q3: 路徑中包含中文字元
**問題**：路徑 `C:\Users\warre\OneDrive\文件\...` 包含中文「文件」

**如果出現編碼問題**：
1. 將專案移到純英文路徑，例如：`C:\Projects\MyJavaProject`
2. 更新 mcp.json 中的路徑

---

### Q4: Node.js 版本問題
**檢查版本**：
```powershell
node --version
```

**要求**：v18 或更高

**如果版本太舊**：
前往 https://nodejs.org/ 下載最新 LTS 版本

---

## 完整的啟動流程

### 每次使用前的檢查

```powershell
# Terminal 1: 啟動 PostgreSQL (如果未運行)
docker start <postgres_container_id>

# Terminal 2: 啟動 Spring Boot
cd C:\Users\warre\OneDrive\文件\MyJavaProject
./mvnw.cmd spring-boot:run

# 等待 Spring Boot 完全啟動（看到 "Started MyJavaProjectApplication"）

# 然後啟動 Claude Desktop
# MCP Server 會自動啟動
```

---

## 驗證整合成功

### 測試案例

**測試 1**：搜尋景點
```
在 Claude Desktop 輸入：「幫我找士林區的景點」
```

**成功指標**：
- ✅ Claude 回傳包含景點名稱、地址、分類的資料
- ✅ 資料來自你的 PostgreSQL 資料庫（不是網路搜尋）

**測試 2**：搜尋美食
```
在 Claude Desktop 輸入：「推薦台北市信義區的餐廳」
```

**測試 3**：搜尋活動
```
在 Claude Desktop 輸入：「有什麼進行中的節慶活動？」
```

---

## 進階除錯

### 啟用 MCP Server 日誌

修改 `mcp.json`，加入日誌輸出：

```json
{
  "mcpServers": {
    "taiwan-travel": {
      "command": "node",
      "args": ["C:\\Users\\warre\\OneDrive\\文件\\MyJavaProject\\mcp-server\\dist\\index.js"],
      "env": {
        "JAVA_API_URL": "http://localhost:8080",
        "DEBUG": "*"
      }
    }
  }
}
```

重啟 Claude Desktop 後，MCP Server 的日誌會輸出更詳細的資訊。

---

## 聯絡支援

如果以上步驟都無法解決問題：

1. 檢查 Claude Desktop 版本（確保是最新版）
2. 查看 Anthropic 官方文件：https://docs.claude.com/
3. 到 GitHub Issues 回報問題

---

**祝使用順利！** 🎉
