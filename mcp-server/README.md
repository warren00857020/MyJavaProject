# Taiwan Travel MCP Server

這是一個 Model Context Protocol (MCP) Server，讓 Claude AI 能直接與你的台灣旅遊 Spring Boot API 互動。

## 🎯 功能

### 可用的 Tools

1. **search_sights** - 搜尋景點
   - 參數：zone（行政區）, keyword（關鍵字）, category（分類）, limit（數量限制）
   - 範例：`搜尋士林區的博物館`

2. **search_foods** - 搜尋美食
   - 參數：query（搜尋關鍵字）, maxResults（最多結果數）
   - 範例：`搜尋台北市的牛肉麵`

3. **search_festivals** - 搜尋節慶活動
   - 參數：status（ongoing/upcoming/all）, zone（區域）, keyword（關鍵字）
   - 範例：`查詢進行中的活動`

4. **get_sight_details** - 取得景點詳細資訊
   - 參數：sightId（景點 ID）

5. **get_food_details** - 取得餐廳詳細資訊
   - 參數：foodId（餐廳 ID）

## 📦 安裝步驟

### 1. 安裝 Node.js 依賴

```bash
cd mcp-server
npm install
```

### 2. 編譯 TypeScript

```bash
npm run build
```

### 3. 設定環境變數（可選）

```bash
# Windows
set JAVA_API_URL=http://localhost:8080

# Linux/Mac
export JAVA_API_URL=http://localhost:8080
```

## 🚀 使用方式

### 方法 1：開發模式（推薦測試用）

```bash
npm run dev
```

### 方法 2：生產模式

```bash
npm run build
npm start
```

## 🔧 與 Claude Desktop 整合

### Windows

編輯檔案：`%APPDATA%\.claude\mcp.json`

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

### Mac/Linux

編輯檔案：`~/.claude/mcp.json`

```json
{
  "mcpServers": {
    "taiwan-travel": {
      "command": "node",
      "args": ["/absolute/path/to/MyJavaProject/mcp-server/dist/index.js"],
      "env": {
        "JAVA_API_URL": "http://localhost:8080"
      }
    }
  }
}
```

## 📝 測試

### 1. 確保 Spring Boot 服務正在運行

```bash
cd ..
./mvnw.cmd spring-boot:run
```

### 2. 啟動 MCP Server

```bash
cd mcp-server
npm run dev
```

### 3. 在 Claude Desktop 中測試

重啟 Claude Desktop，然後嘗試：

```
使用者：「幫我找士林區的景點」
Claude 會自動呼叫：search_sights(zone="士林區")

使用者：「搜尋台北市的牛肉麵」
Claude 會自動呼叫：search_foods(query="台北市 牛肉麵")

使用者：「有什麼進行中的活動？」
Claude 會自動呼叫：search_festivals(status="ongoing")
```

## 🛠️ 開發說明

### 專案結構

```
mcp-server/
├── src/
│   └── index.ts          # MCP Server 主程式
├── dist/                 # 編譯後的 JavaScript
├── package.json
├── tsconfig.json
└── README.md
```

### 修改 Tools

編輯 `src/index.ts`，在 `tools` 陣列中新增或修改工具定義。

### 除錯

1. 檢查 Spring Boot 服務是否運行：`curl http://localhost:8080/sights`
2. 檢查 MCP Server 是否正常啟動：查看終端機輸出
3. 檢查 Claude Desktop 設定：`%APPDATA%\.claude\mcp.json`

## 📚 相關文件

- [MCP 協議規格](https://modelcontextprotocol.io)
- [Claude Agent SDK](https://platform.claude.com/docs/en/agent-sdk/overview)
- [Spring Boot API 文件](../docs/CONTROLLER_API_REFERENCE.md)

## 🐛 常見問題

### Q: Claude Desktop 找不到 MCP Server？
A: 檢查：
1. `mcp.json` 檔案路徑是否正確
2. `args` 中的路徑是否為絕對路徑
3. 重啟 Claude Desktop

### Q: Tools 無法呼叫 Java API？
A: 檢查：
1. Spring Boot 服務是否運行在 `http://localhost:8080`
2. 防火牆是否阻擋連線
3. 檢查 MCP Server 終端機的錯誤訊息

### Q: 如何新增更多 Tools？
A: 編輯 `src/index.ts`，參考現有的 tool 定義，新增你的 tool。

## 📄 授權

MIT
