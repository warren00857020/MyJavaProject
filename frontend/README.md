# Taiwan Travel AI Assistant - 前端介面

簡單的聊天介面，用於與 Taiwan Travel AI 後端 API 互動。

## 功能特色

- 🎨 現代化的聊天介面
- 💬 支援對話歷史記錄
- ⚙️ 可自訂 API 端點（本地/Railway）
- 📱 響應式設計（RWD）
- 🚀 純 HTML/CSS/JS，無需 build

## 快速開始

### 1. 啟動後端 API

```bash
# 確保你的 Spring Boot 後端正在運行
cd c:\Users\warre\OneDrive\文件\MyJavaProject
.\mvnw.cmd spring-boot:run
```

### 2. 開啟前端

有兩種方式：

**方式 A：直接開啟 HTML（最簡單）**
```bash
# 直接用瀏覽器開啟
start frontend/index.html
```

**方式 B：使用 HTTP 伺服器（推薦）**
```bash
# 使用 Python 簡單伺服器
cd frontend
python -m http.server 3000
# 然後開啟 http://localhost:3000
```

或使用 Node.js：
```bash
# 安裝 http-server
npm install -g http-server

# 啟動伺服器
cd frontend
http-server -p 3000
# 然後開啟 http://localhost:3000
```

### 3. 設定 API 端點

點擊右下角的 ⚙️ 設定按鈕，選擇：

- **本地開發**: `http://localhost:8080/api/chat`
- **Railway 部署**: `https://your-app.railway.app/api/chat`

## 環境需求

### 後端設定

確保你的後端 `.env` 檔案包含：

```env
CLAUDE_API_KEY=sk-ant-your-api-key
```

## 部署到 Railway

### 方式 1：部署靜態檔案

1. 在 Railway 新建一個 Static Site service
2. 連接你的 GitHub repo
3. 設定 Root Directory: `frontend`
4. 自動部署！

### 方式 2：使用 Nginx（進階）

建立 `frontend/Dockerfile`:

```dockerfile
FROM nginx:alpine
COPY . /usr/share/nginx/html
EXPOSE 80
```

在 Railway 建立新 service，使用 Dockerfile 部署。

## 檔案結構

```
frontend/
├── index.html      # 主頁面
├── style.css       # 樣式表
├── script.js       # JavaScript 邏輯
└── README.md       # 說明文件
```

## 使用範例

試試問 AI：

- 「推薦台北的景點」
- 「台中有什麼好吃的？」
- 「幫我規劃台南一日遊」
- 「高雄哪裡適合看夕陽？」

## 問題排除

### 無法連接到 API

1. 確認後端是否正在運行
2. 檢查 API 端點設定是否正確
3. 確認 CORS 設定（後端已設定 `@CrossOrigin(origins = "*")`）

### Claude API Key 錯誤

確保在 Railway 的環境變數中設定了 `CLAUDE_API_KEY`

## 客製化

### 修改主題顏色

在 `style.css` 中修改：

```css
background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
```

### 修改歡迎訊息

在 `index.html` 中修改 Welcome Message 區塊。

## 授權

MIT License
