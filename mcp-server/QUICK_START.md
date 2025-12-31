# 🚀 MCP Server 快速啟動指南

## ✅ 前置條件

1. **Node.js** - 需要 v18 或更高版本
   - 檢查版本：`node --version`
   - 下載：https://nodejs.org/

2. **Spring Boot 服務** - 必須在運行中
   - 啟動：`cd .. && ./mvnw.cmd spring-boot:run`
   - 測試：開啟 http://localhost:8080/sights

## 📦 安裝步驟（只需執行一次）

```bash
# 1. 進入 MCP Server 目錄
cd C:\Users\warre\OneDrive\文件\MyJavaProject\mcp-server

# 2. 安裝依賴
npm install

# 3. 編譯 TypeScript
npm run build
```

## 🎯 啟動 MCP Server

### 開發模式（推薦）
```bash
npm run dev
```

看到以下訊息表示成功：
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

## 🔗 與 Claude Desktop 整合

### 步驟 1：找到設定檔

**Windows**：
```
%APPDATA%\.claude\mcp.json
```
完整路徑通常是：
```
C:\Users\warre\AppData\Roaming\.claude\mcp.json
```

**如果檔案不存在**，手動建立：
1. 建立資料夾：`C:\Users\warre\AppData\Roaming\.claude`
2. 建立檔案：`mcp.json`

### 步驟 2：編輯設定檔

貼上以下內容（**注意：修改路徑為你的實際路徑**）：

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

⚠️ **重要**：
- 路徑必須使用 `\\` (雙反斜線)
- 必須是絕對路徑
- 檢查 `dist/index.js` 是否存在

### 步驟 3：重啟 Claude Desktop

完全關閉並重新開啟 Claude Desktop。

### 步驟 4：驗證整合

在 Claude Desktop 中輸入：

```
幫我找士林區的景點
```

如果 Claude 回應包含景點資訊，表示整合成功！🎉

## 🧪 測試案例

### 測試 1：搜尋景點
```
使用者：「找台北市中正區的景點」
預期：Claude 回傳中正區的景點列表
```

### 測試 2：搜尋美食
```
使用者：「幫我找台北市的牛肉麵」
預期：Claude 回傳台北牛肉麵餐廳列表
```

### 測試 3：搜尋活動
```
使用者：「有什麼進行中的節慶活動？」
預期：Claude 回傳目前進行中的活動
```

### 測試 4：複雜查詢
```
使用者：「幫我規劃士林區一日遊，包含景點和美食推薦」
預期：Claude 會：
1. 呼叫 search_sights(zone="士林區")
2. 呼叫 search_foods(query="士林區 美食")
3. 整理成行程建議
```

## 🐛 故障排除

### 問題 1：`npm install` 失敗

**解決方案**：
```bash
# 清除 npm 快取
npm cache clean --force

# 刪除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安裝
npm install
```

### 問題 2：Claude Desktop 找不到 MCP Server

**檢查清單**：
- [ ] `mcp.json` 路徑正確（`%APPDATA%\.claude\mcp.json`）
- [ ] `args` 中的路徑為絕對路徑
- [ ] `dist/index.js` 檔案存在（執行過 `npm run build`）
- [ ] 已重啟 Claude Desktop

**除錯步驟**：
```bash
# 1. 確認檔案存在
dir C:\Users\warre\OneDrive\文件\MyJavaProject\mcp-server\dist\index.js

# 2. 手動測試執行
node C:\Users\warre\OneDrive\文件\MyJavaProject\mcp-server\dist\index.js
```

### 問題 3：Tools 無法呼叫 Java API

**檢查清單**：
- [ ] Spring Boot 服務運行中（`http://localhost:8080`）
- [ ] 測試 API：`curl http://localhost:8080/sights`
- [ ] 防火牆沒有阻擋

**測試連線**：
```bash
# Windows PowerShell
Invoke-WebRequest -Uri http://localhost:8080/sights

# 或在瀏覽器開啟
# http://localhost:8080/sights
```

### 問題 4：TypeScript 編譯錯誤

**解決方案**：
```bash
# 確認 TypeScript 版本
npx tsc --version

# 重新安裝 TypeScript
npm install --save-dev typescript@latest

# 重新編譯
npm run build
```

## 📊 完整的運行流程

1. **啟動 Spring Boot**
   ```bash
   cd C:\Users\warre\OneDrive\文件\MyJavaProject
   ./mvnw.cmd spring-boot:run
   ```
   ✅ 確認：瀏覽器開啟 http://localhost:8080/sights

2. **啟動 MCP Server**（新開一個終端機）
   ```bash
   cd C:\Users\warre\OneDrive\文件\MyJavaProject\mcp-server
   npm run dev
   ```
   ✅ 確認：看到 "Taiwan Travel MCP Server 已啟動"

3. **設定 Claude Desktop**
   - 編輯 `%APPDATA%\.claude\mcp.json`
   - 貼上設定
   - 重啟 Claude Desktop

4. **開始使用**
   - 在 Claude Desktop 輸入旅遊相關問題
   - 觀察 MCP Server 終端機的 API 呼叫記錄

## 🎉 成功指標

✅ **MCP Server 啟動成功**
- 終端機顯示 "Taiwan Travel MCP Server 已啟動"
- 沒有錯誤訊息

✅ **Claude Desktop 整合成功**
- Claude 能回應景點/美食/活動查詢
- 回應包含資料庫中的真實資料

✅ **完整系統運作**
- 使用者提問 → Claude 呼叫 Tools → MCP Server 轉發 → Spring Boot API → PostgreSQL
- 整個流程無縫運作

## 📚 下一步

完成基礎整合後，可以：

1. **新增更多 Tools** - 行程規劃、路線計算
2. **優化回應格式** - 讓 Claude 回傳更友善的資訊
3. **加入快取** - 減少 API 呼叫次數
4. **監控與日誌** - 追蹤 API 使用情況

祝使用順利！🚀
