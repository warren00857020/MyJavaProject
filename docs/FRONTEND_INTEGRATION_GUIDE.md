# 前端整合指南

## 架構概述

前端可以**直接呼叫 Spring Boot API**，完全獨立於 MCP Server 和 Claude。

```
┌─────────────┐
│   前端應用   │ (React/Vue/Next.js/原生 HTML)
│             │
│  - 景點搜尋  │
│  - 美食探索  │
│  - 活動查詢  │
└──────┬──────┘
       │ HTTP REST API
       ↓
┌─────────────────┐
│  Spring Boot    │
│  (Port 8080)    │
│                 │
│  Controllers:   │
│  - /sights      │
│  - /foods       │
│  - /festivals   │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│   PostgreSQL    │
│   (Port 5432)   │
└─────────────────┘

註：MCP Server 和 Claude 是獨立的，僅供 AI 助手使用
```

---

## API 端點總覽

### 基礎 URL
```
http://localhost:8080
```

### 1. 景點 API

#### 搜尋景點
```http
GET /sights?zone={區域}&keyword={關鍵字}&category={分類}
```

**範例**：
```javascript
// 搜尋士林區的景點
fetch('http://localhost:8080/sights?zone=士林區')
  .then(res => res.json())
  .then(data => console.log(data));

// 搜尋包含「博物館」的景點
fetch('http://localhost:8080/sights?keyword=博物館')
  .then(res => res.json())
  .then(data => console.log(data));
```

**回應格式**：
```json
[
  {
    "id": 1,
    "sightName": "國立故宮博物院",
    "zone": "士林區",
    "category": "博物館",
    "photoUrl": "https://...",
    "description": "...",
    "address": "台北市士林區至善路二段221號",
    "ticketPrice": "350元",
    "phone": "02-2881-2021",
    "sourceUrl": "https://..."
  }
]
```

#### 取得景點詳情
```http
GET /sights/{id}
```

**範例**：
```javascript
fetch('http://localhost:8080/sights/1')
  .then(res => res.json())
  .then(data => console.log(data));
```

---

### 2. 美食 API

#### 搜尋餐廳
```http
GET /foods?zone={區域}&keyword={關鍵字}
```

**範例**：
```javascript
// 搜尋信義區的餐廳
fetch('http://localhost:8080/foods?zone=信義區')
  .then(res => res.json())
  .then(data => console.log(data));

// 搜尋拉麵店
fetch('http://localhost:8080/foods?keyword=拉麵')
  .then(res => res.json())
  .then(data => console.log(data));
```

**回應格式**：
```json
[
  {
    "id": 1,
    "name": "鼎泰豐",
    "zone": "信義區",
    "address": "台北市信義區市府路45號",
    "rating": 4.5,
    "userRatingsTotal": 2000,
    "priceLevel": 2,
    "openingHours": {
      "open_now": true,
      "weekday_text": [
        "星期一: 11:00 – 21:00",
        "星期二: 11:00 – 21:00",
        ...
      ]
    },
    "types": ["restaurant", "food"],
    "photoUrls": ["https://..."]
  }
]
```

#### 取得餐廳完整資訊
```http
GET /foods/{id}/details
```

**範例**：
```javascript
fetch('http://localhost:8080/foods/1/details')
  .then(res => res.json())
  .then(data => console.log(data));
```

---

### 3. 活動 API

#### 進行中的活動
```http
GET /festivals/ongoing
```

**範例**：
```javascript
fetch('http://localhost:8080/festivals/ongoing')
  .then(res => res.json())
  .then(data => console.log(data));
```

#### 即將開始的活動
```http
GET /festivals/upcoming?days={天數}
```

**範例**：
```javascript
// 未來 30 天的活動
fetch('http://localhost:8080/festivals/upcoming?days=30')
  .then(res => res.json())
  .then(data => console.log(data));
```

#### 特定區域的活動
```http
GET /festivals/zone/{zone}
```

**範例**：
```javascript
fetch('http://localhost:8080/festivals/zone/板橋區')
  .then(res => res.json())
  .then(data => console.log(data));
```

#### 搜尋活動
```http
GET /festivals/search?keyword={關鍵字}
```

**範例**：
```javascript
fetch('http://localhost:8080/festivals/search?keyword=音樂')
  .then(res => res.json())
  .then(data => console.log(data));
```

**回應格式**：
```json
[
  {
    "id": 1,
    "festivalName": "2025台北燈節",
    "zone": "中正區",
    "startDate": "2025-02-01",
    "endDate": "2025-02-15",
    "address": "台北市中正區重慶南路一段122號",
    "description": "...",
    "photoUrls": "https://...,https://...",
    "organizer": "台北市政府",
    "website": "https://...",
    "tags": "燈節,藝術,節慶"
  }
]
```

---

## CORS 設定

如果你的前端運行在不同的 port（例如 `http://localhost:3000`），需要在 Spring Boot 中啟用 CORS。

### 檢查是否已設定 CORS

查看是否有 `WebConfig.java` 或 CORS 相關設定。如果沒有，需要新增。

### 新增 CORS 設定（如需要）

建立 `src/main/java/com/example/config/WebConfig.java`：

```java
package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173") // Vite/React 預設 ports
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

---

## 前端框架範例

### React 範例

```jsx
import { useState, useEffect } from 'react';

function SightList() {
  const [sights, setSights] = useState([]);
  const [zone, setZone] = useState('');

  const searchSights = async () => {
    const url = zone
      ? `http://localhost:8080/sights?zone=${zone}`
      : 'http://localhost:8080/sights';

    const response = await fetch(url);
    const data = await response.json();
    setSights(data);
  };

  useEffect(() => {
    searchSights();
  }, []);

  return (
    <div>
      <h1>台灣景點搜尋</h1>
      <input
        value={zone}
        onChange={(e) => setZone(e.target.value)}
        placeholder="輸入區域（例如：士林區）"
      />
      <button onClick={searchSights}>搜尋</button>

      <div>
        {sights.map(sight => (
          <div key={sight.id}>
            <h3>{sight.sightName}</h3>
            <p>{sight.zone} - {sight.category}</p>
            <p>{sight.description}</p>
            <img src={sight.photoUrl} alt={sight.sightName} />
          </div>
        ))}
      </div>
    </div>
  );
}

export default SightList;
```

### Vue 範例

```vue
<template>
  <div>
    <h1>台灣景點搜尋</h1>
    <input v-model="zone" placeholder="輸入區域（例如：士林區）">
    <button @click="searchSights">搜尋</button>

    <div v-for="sight in sights" :key="sight.id">
      <h3>{{ sight.sightName }}</h3>
      <p>{{ sight.zone }} - {{ sight.category }}</p>
      <p>{{ sight.description }}</p>
      <img :src="sight.photoUrl" :alt="sight.sightName">
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const sights = ref([]);
const zone = ref('');

const searchSights = async () => {
  const url = zone.value
    ? `http://localhost:8080/sights?zone=${zone.value}`
    : 'http://localhost:8080/sights';

  const response = await fetch(url);
  const data = await response.json();
  sights.value = data;
};

onMounted(() => {
  searchSights();
});
</script>
```

### 原生 HTML + JavaScript 範例

```html
<!DOCTYPE html>
<html lang="zh-TW">
<head>
  <meta charset="UTF-8">
  <title>台灣旅遊景點</title>
  <style>
    body {
      font-family: Arial, sans-serif;
      max-width: 1200px;
      margin: 0 auto;
      padding: 20px;
    }
    .search-box {
      margin-bottom: 20px;
    }
    .sight-card {
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
    }
    .sight-card img {
      max-width: 100%;
      border-radius: 4px;
    }
  </style>
</head>
<body>
  <h1>🗺️ 台灣旅遊景點</h1>

  <div class="search-box">
    <input type="text" id="zone" placeholder="輸入區域（例如：士林區）">
    <button onclick="searchSights()">搜尋景點</button>

    <input type="text" id="keyword" placeholder="輸入關鍵字（例如：博物館）">
    <button onclick="searchByKeyword()">關鍵字搜尋</button>
  </div>

  <div id="results"></div>

  <script>
    const API_BASE = 'http://localhost:8080';

    async function searchSights() {
      const zone = document.getElementById('zone').value;
      const url = zone
        ? `${API_BASE}/sights?zone=${encodeURIComponent(zone)}`
        : `${API_BASE}/sights`;

      try {
        const response = await fetch(url);
        const sights = await response.json();
        displaySights(sights);
      } catch (error) {
        console.error('搜尋失敗:', error);
        alert('搜尋失敗，請確認 Spring Boot 服務是否正在運行');
      }
    }

    async function searchByKeyword() {
      const keyword = document.getElementById('keyword').value;
      const url = `${API_BASE}/sights?keyword=${encodeURIComponent(keyword)}`;

      try {
        const response = await fetch(url);
        const sights = await response.json();
        displaySights(sights);
      } catch (error) {
        console.error('搜尋失敗:', error);
      }
    }

    function displaySights(sights) {
      const resultsDiv = document.getElementById('results');

      if (sights.length === 0) {
        resultsDiv.innerHTML = '<p>查無結果</p>';
        return;
      }

      resultsDiv.innerHTML = sights.map(sight => `
        <div class="sight-card">
          <h3>${sight.sightName}</h3>
          <p><strong>區域：</strong>${sight.zone}</p>
          <p><strong>分類：</strong>${sight.category || '未分類'}</p>
          <p><strong>地址：</strong>${sight.address || '無'}</p>
          <p><strong>票價：</strong>${sight.ticketPrice || '免費'}</p>
          <p><strong>電話：</strong>${sight.phone || '無'}</p>
          <p>${sight.description || ''}</p>
          ${sight.photoUrl ? `<img src="${sight.photoUrl}" alt="${sight.sightName}">` : ''}
          ${sight.sourceUrl ? `<a href="${sight.sourceUrl}" target="_blank">詳細資訊</a>` : ''}
        </div>
      `).join('');
    }

    // 頁面載入時自動搜尋
    window.onload = searchSights;
  </script>
</body>
</html>
```

---

## 完整的旅遊網站範例

### 建議的頁面結構

```
前端專案/
├── pages/
│   ├── index.html          # 首頁
│   ├── sights.html         # 景點列表
│   ├── foods.html          # 美食列表
│   ├── festivals.html      # 活動列表
│   └── detail.html         # 詳細頁面
├── js/
│   ├── api.js              # API 呼叫封裝
│   └── utils.js            # 工具函數
└── css/
    └── style.css           # 樣式
```

### API 封裝範例 (api.js)

```javascript
const API_BASE = 'http://localhost:8080';

export const sightApi = {
  search: async (params = {}) => {
    const queryString = new URLSearchParams(params).toString();
    const response = await fetch(`${API_BASE}/sights?${queryString}`);
    return response.json();
  },

  getById: async (id) => {
    const response = await fetch(`${API_BASE}/sights/${id}`);
    return response.json();
  }
};

export const foodApi = {
  search: async (params = {}) => {
    const queryString = new URLSearchParams(params).toString();
    const response = await fetch(`${API_BASE}/foods?${queryString}`);
    return response.json();
  },

  getDetails: async (id) => {
    const response = await fetch(`${API_BASE}/foods/${id}/details`);
    return response.json();
  }
};

export const festivalApi = {
  ongoing: async () => {
    const response = await fetch(`${API_BASE}/festivals/ongoing`);
    return response.json();
  },

  upcoming: async (days = 30) => {
    const response = await fetch(`${API_BASE}/festivals/upcoming?days=${days}`);
    return response.json();
  },

  search: async (keyword) => {
    const response = await fetch(`${API_BASE}/festivals/search?keyword=${encodeURIComponent(keyword)}`);
    return response.json();
  }
};
```

---

## 測試 API

### 使用瀏覽器測試

直接在瀏覽器中開啟：
```
http://localhost:8080/sights
http://localhost:8080/foods
http://localhost:8080/festivals/ongoing
```

### 使用 curl 測試

```bash
# 測試景點 API
curl http://localhost:8080/sights?zone=士林區

# 測試美食 API
curl http://localhost:8080/foods?keyword=拉麵

# 測試活動 API
curl http://localhost:8080/festivals/ongoing
```

### 使用 Postman 測試

1. 開啟 Postman
2. 建立新的 GET 請求
3. 輸入 URL：`http://localhost:8080/sights`
4. 點擊 Send

---

## 部署注意事項

### 開發環境
- Spring Boot: `http://localhost:8080`
- 前端開發伺服器: `http://localhost:3000` (React/Vue)

### 生產環境
- 需要設定正確的 API URL（環境變數）
- 啟用 HTTPS
- 設定正確的 CORS 來源
- 考慮使用 Nginx 作為反向代理

---

## 總結

### ✅ 前端可以做的事
- 直接呼叫所有 Spring Boot REST API
- 完全獨立於 Claude 和 MCP Server
- 使用任何前端框架（React, Vue, Angular, Next.js 等）

### ❌ 前端不需要做的事
- 不需要安裝 MCP Server
- 不需要使用 Claude Desktop
- 不需要處理 JSON-RPC 協議

### 🎯 MCP Server 的用途
- **僅供 Claude AI 使用**
- 讓 Claude Desktop/Code 可以查詢你的旅遊資料
- 與前端完全獨立，互不影響

---

**開始建立你的前端應用吧！** 🚀
