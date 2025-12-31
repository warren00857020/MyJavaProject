# Controller API 參考文件

## 📚 概述

本專案包含 4 個核心 Controller，提供景點、美食、排程和 API 測試功能。

---

## 1️⃣ SightController - 景點管理

**路徑**: `src/main/java/com/example/controller/SightController.java`

### 🔧 爬蟲相關 API

#### 1.1 爬取單一縣市的景點
```http
POST /sights/crawler/region?region=TPE
```

**參數**:
- `region` (String): 縣市代碼（例：TPE, TXG, KHH）

**回應範例**:
```json
{
  "success": true,
  "regionCode": "TPE",
  "itemsProcessed": 25,
  "message": "成功爬取 25 個景點"
}
```

---

#### 1.2 爬取所有縣市的景點
```http
POST /sights/crawler/all
```

**回應範例**:
```json
{
  "success": true,
  "message": "完成爬取",
  "totalRegions": 22,
  "successRegions": 22,
  "totalItems": 450,
  "details": [...]
}
```

---

#### 1.3 批次爬取多個縣市
```http
POST /sights/crawler/batch
Content-Type: application/json
```

**Request Body**:
```json
{
  "regions": ["TPE", "TXG", "KHH"]
}
```

**回應範例**:
```json
{
  "success": true,
  "message": "完成批次爬取",
  "totalRegions": 3,
  "successRegions": 3,
  "totalItems": 75
}
```

---

#### 1.4 直接爬取指定 URL 的景點
```http
POST /sights/crawler/url
Content-Type: application/json
```

**Request Body**:
```json
{
  "url": "https://www.travelking.com.tw/tourguide/scenery1397.html"
}
```

**回應範例**:
```json
{
  "success": true,
  "sight": {
    "sightName": "台北101",
    "zone": "信義區",
    ...
  },
  "message": "成功爬取景點"
}
```

---

#### 1.5 批次爬取多個 URL
```http
POST /sights/crawler/urls
Content-Type: application/json
```

**Request Body**:
```json
{
  "urls": [
    "https://www.travelking.com.tw/tourguide/scenery1397.html",
    "https://www.travelking.com.tw/tourguide/scenery1398.html"
  ]
}
```

**回應範例**:
```json
{
  "success": true,
  "totalUrls": 2,
  "successCount": 2,
  "failedCount": 0,
  "successSights": [...],
  "failedUrls": []
}
```

---

#### 1.6 爬取指定城市（舊版 API，已棄用）
```http
GET /setSights/{city}
```

**⚠️ 已棄用**: 請改用 `POST /sights/crawler/region`

---

### 📝 景點 CRUD API

#### 1.7 新增景點
```http
POST /sights
Content-Type: application/json
```

**Request Body**:
```json
{
  "sightName": "中正紀念堂",
  "zone": "中正區",
  "category": "文化古蹟",
  "description": "台北著名地標",
  "address": "台北市中正區中山南路21號",
  "latitude": 25.0408,
  "longitude": 121.5181
}
```

---

#### 1.8 查詢單一景點
```http
GET /sights/{sightName}
```

**回應範例**:
```json
{
  "sightName": "中正紀念堂",
  "zone": "中正區",
  "category": "文化古蹟",
  ...
}
```

---

#### 1.9 查詢所有景點 / 根據區域查詢
```http
GET /sights
GET /sights?keyword=中正區
```

**參數**:
- `keyword` (可選): 行政區名稱

**回應**: 景點列表

---

#### 1.10 刪除景點
```http
DELETE /sights/{sightName}
```

**回應**: 204 No Content

---

## 2️⃣ FoodController - 美食管理

**路徑**: `src/main/java/com/example/controller/FoodController.java`

### 🍽️ Google Places API 相關

#### 2.1 文字搜尋並儲存餐廳
```http
POST /foods/crawl-by-text?query=台北市信義區 餐廳&maxResults=20
```

**參數**:
- `query` (String): 搜尋關鍵字
- `maxResults` (Integer, 預設=20): 最多結果數

**回應範例**:
```json
{
  "success": true,
  "query": "台北市信義區 餐廳",
  "totalFound": 20,
  "totalSaved": 18,
  "foods": [...]
}
```

---

#### 2.2 搜尋景點周邊餐廳
```http
POST /foods/crawl-by-sight/{sightId}?radius=500&maxResults=20
```

**參數**:
- `sightId` (Long): 景點 ID
- `radius` (Integer, 預設=500): 搜尋半徑（公尺）
- `maxResults` (Integer, 預設=20): 最多結果數

---

#### 2.3 獲取單一餐廳詳細資訊（含完整營業時間）
```http
GET /foods/{id}/details
```

**功能**: 使用 Place Details API 補充完整營業時間、電話、網站

**回應範例**:
```json
{
  "success": true,
  "message": "已更新完整營業時間、電話和網站資訊",
  "food": {
    "id": 1,
    "foodName": "默爾 pasta pizza",
    "phone": "02 2723 0073",
    "officialWebsite": "https://...",
    "openingHours": "{\"weekday_text\":[...]}"
  }
}
```

---

#### 2.4 批次補充所有餐廳詳細資訊
```http
POST /foods/enrich-all?limit=10
```

**參數**:
- `limit` (Integer, 預設=10): 處理數量限制

**回應範例**:
```json
{
  "success": true,
  "total": 50,
  "processed": 10,
  "success_count": 10,
  "failed_count": 0,
  "message": "已處理 10/50 筆餐廳資料"
}
```

---

### 📋 餐廳查詢 API

#### 2.5 查詢所有餐廳
```http
GET /foods
```

---

#### 2.6 根據行政區查詢
```http
GET /foods/zone/{zone}
```

**範例**: `GET /foods/zone/信義區`

---

#### 2.7 根據關鍵字搜尋
```http
GET /foods/search?keyword=火鍋
```

---

#### 2.8 測試 Google Places API
```http
GET /foods/test-api?lat=25.033&lng=121.565&radius=500&maxResults=10
```

---

## 3️⃣ SchedulerController - 排程管理

**路徑**: `src/main/java/com/example/controller/SchedulerController.java`

### ⏰ 排程控制 API

#### 3.1 手動觸發餐廳資料更新
```http
POST /scheduler/update-foods
```

**功能**: 立即執行餐廳資料批次更新（台北市 12 區）

**回應範例**:
```json
{
  "success": true,
  "message": "餐廳資料更新任務已啟動，請查看日誌"
}
```

---

#### 3.2 查看排程狀態
```http
GET /scheduler/status
```

**回應範例**:
```json
{
  "schedulerEnabled": true,
  "weeklyUpdateCron": "0 0 2 * * SUN",
  "description": "每週日凌晨 2:00 自動更新餐廳資料"
}
```

---

## 4️⃣ ApiTestController - API 測試工具

**路徑**: `src/main/java/com/example/controller/ApiTestController.java`

### 🧪 診斷測試 API

#### 4.1 直接測試 Google Places API（複製 curl 成功的 URL）
```http
GET /api-test/test-direct
```

**功能**: 使用固定的測試 URL 直接呼叫 Google Places API

**回應**: 完整的 API 回應內容

---

#### 4.2 測試英文查詢
```http
GET /api-test/test-english
```

**功能**: 測試英文關鍵字搜尋

---

## 📊 API 路徑總覽

### 景點相關
```
POST   /sights/crawler/region      爬取單一縣市
POST   /sights/crawler/all          爬取所有縣市
POST   /sights/crawler/batch        批次爬取多個縣市
POST   /sights/crawler/url          爬取單一 URL
POST   /sights/crawler/urls         批次爬取多個 URL
GET    /setSights/{city}            (已棄用) 爬取指定城市

POST   /sights                      新增景點
GET    /sights/{sightName}          查詢單一景點
GET    /sights                      查詢所有景點
GET    /sights?keyword=區域         根據區域查詢
DELETE /sights/{sightName}          刪除景點
```

### 美食相關
```
POST   /foods/crawl-by-text         文字搜尋餐廳
POST   /foods/crawl-by-sight/{id}   景點周邊餐廳
GET    /foods/{id}/details          獲取詳細資訊
POST   /foods/enrich-all            批次補充詳細資訊

GET    /foods                       查詢所有餐廳
GET    /foods/zone/{zone}           根據區域查詢
GET    /foods/search?keyword=       關鍵字搜尋
GET    /foods/test-api              測試 Google API
```

### 排程相關
```
POST   /scheduler/update-foods      手動觸發更新
GET    /scheduler/status            查看排程狀態
```

### API 測試
```
GET    /api-test/test-direct        直接測試 API
GET    /api-test/test-english       英文查詢測試
```

---

## 🔄 重構說明

### 移除的 Controller
- ❌ **CrawlerController** - 功能已合併到 SightController
- ❌ **ManualCrawlerController** - 功能已合併到 SightController（`/sights/crawler/url` 和 `/sights/crawler/urls`）

### 保留的 Controller
- ✅ **SightController** - 景點 CRUD + 爬蟲功能（整合後）
- ✅ **FoodController** - 美食 CRUD + Google Places API
- ✅ **SchedulerController** - 排程管理
- ✅ **ApiTestController** - API 診斷工具

---

## 🚀 快速開始範例

### 爬取台北市景點
```bash
curl -X POST "http://localhost:8080/sights/crawler/region?region=TPE"
```

### 搜尋信義區餐廳
```bash
curl -X POST "http://localhost:8080/foods/crawl-by-text?query=台北市信義區%20餐廳&maxResults=10"
```

### 獲取餐廳詳細資訊
```bash
curl http://localhost:8080/foods/1/details
```

### 手動觸發排程
```bash
curl -X POST http://localhost:8080/scheduler/update-foods
```

---

**最後更新**: 2025-12-26
**版本**: 2.0（重構後）
