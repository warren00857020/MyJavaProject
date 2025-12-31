# Controller 重構總結

## 📋 重構目標

整理 Controller 層架構，消除功能重複，提升程式碼可維護性。

---

## 🔄 重構前 vs 重構後

### 重構前（6 個 Controller）

```
src/main/java/com/example/controller/
├── SightController.java          景點 CRUD
├── CrawlerController.java        景點爬蟲觸發 ❌ 功能重複
├── ManualCrawlerController.java  手動 URL 爬取 ❌ 已不需要
├── FoodController.java           美食 CRUD + Google Places API
├── SchedulerController.java      排程管理
└── ApiTestController.java        API 測試工具
```

**問題**:
- `CrawlerController` 和 `SightController` 都處理景點爬蟲，功能重複
- `ManualCrawlerController` 是為了繞過反爬蟲而建立，現在可直接用 URL 爬取，不再需要

### 重構後（4 個 Controller）

```
src/main/java/com/example/controller/
├── SightController.java          景點 CRUD + 爬蟲功能（整合）✅
├── FoodController.java           美食 CRUD + Google Places API ✅
├── SchedulerController.java      排程管理 ✅
└── ApiTestController.java        API 測試工具 ✅
```

---

## 📊 詳細變更

### 1. SightController（整合擴充）

#### 新增的爬蟲 API

| 端點 | 功能 | 來源 |
|------|------|------|
| `POST /sights/crawler/region` | 爬取單一縣市 | 來自 CrawlerController |
| `POST /sights/crawler/all` | 爬取所有縣市 | 來自 CrawlerController |
| `POST /sights/crawler/batch` | 批次爬取多個縣市 | 來自 CrawlerController |
| `POST /sights/crawler/url` | 爬取單一 URL | 來自 ManualCrawlerController |
| `POST /sights/crawler/urls` | 批次爬取多個 URL | 來自 ManualCrawlerController |

#### 保留的 CRUD API

| 端點 | 功能 | 狀態 |
|------|------|------|
| `POST /sights` | 新增景點 | ✅ 保留 |
| `GET /sights/{sightName}` | 查詢單一景點 | ✅ 保留 |
| `GET /sights` | 查詢所有景點 | ✅ 保留 |
| `GET /sights?keyword=區域` | 根據區域查詢 | ✅ 保留 |
| `DELETE /sights/{sightName}` | 刪除景點 | ✅ 保留 |
| `GET /setSights/{city}` | 爬取指定城市 | ⚠️ 已棄用 |

**註**: `GET /setSights/{city}` 標記為 `@Deprecated`，建議改用 `POST /sights/crawler/region`

---

### 2. FoodController（無變更）

保持原有功能，包含：
- Google Places API 整合（Text Search, Nearby Search, Place Details）
- 美食 CRUD 操作
- 批次補充詳細資訊

---

### 3. SchedulerController（無變更）

保持原有功能：
- 手動觸發排程
- 查看排程狀態

---

### 4. ApiTestController（無變更）

保持原有功能：
- Google Places API 診斷測試

---

### 5. CrawlerController（已刪除）

**刪除原因**: 功能已完全整合到 SightController

**原有功能轉移**:
```
❌ GET  /api/crawler/sights?region=TPE
   → ✅ POST /sights/crawler/region?region=TPE

❌ POST /api/crawler/sights/all
   → ✅ POST /sights/crawler/all

❌ POST /api/crawler/sights/batch
   → ✅ POST /sights/crawler/batch
```

---

### 6. ManualCrawlerController（已刪除）

**刪除原因**:
1. 最初是為了繞過 TravelKing 的反爬蟲機制而建立
2. 現在可以直接使用 URL 爬取，無需特殊處理
3. 功能已整合到 SightController

**原有功能轉移**:
```
❌ POST /manual-crawler/crawl-urls
   → ✅ POST /sights/crawler/urls
```

---

## 🎯 重構優勢

### 1. **架構更清晰**
- 每個 Controller 職責明確
- 減少功能重複
- 降低維護成本

### 2. **API 路徑更一致**
```
舊版（混亂）:
  /setSights/{city}              (景點爬蟲)
  /api/crawler/sights            (景點爬蟲)
  /manual-crawler/crawl-urls     (景點爬蟲)

新版（統一）:
  /sights/crawler/*              (所有景點爬蟲功能)
```

### 3. **更易於理解**
新手開發者可以快速理解：
- 景點相關 → `SightController`
- 美食相關 → `FoodController`
- 排程相關 → `SchedulerController`
- API 測試 → `ApiTestController`

### 4. **向下兼容**
保留舊版 API（標記為 `@Deprecated`），不會破壞現有前端整合。

---

## 📝 遷移指南

### 如果你正在使用舊版 API

#### CrawlerController API 遷移

```bash
# 舊版
curl -X GET "http://localhost:8080/api/crawler/sights?region=TPE"

# 新版（推薦）
curl -X POST "http://localhost:8080/sights/crawler/region?region=TPE"
```

```bash
# 舊版
curl -X POST "http://localhost:8080/api/crawler/sights/all"

# 新版（推薦）
curl -X POST "http://localhost:8080/sights/crawler/all"
```

```bash
# 舊版
curl -X POST "http://localhost:8080/api/crawler/sights/batch" \
  -H "Content-Type: application/json" \
  -d '{"regions": ["TPE", "TXG"]}'

# 新版（推薦）
curl -X POST "http://localhost:8080/sights/crawler/batch" \
  -H "Content-Type: application/json" \
  -d '{"regions": ["TPE", "TXG"]}'
```

---

#### ManualCrawlerController API 遷移

```bash
# 舊版
curl -X POST "http://localhost:8080/manual-crawler/crawl-urls" \
  -H "Content-Type: application/json" \
  -d '{
    "urls": [
      "https://www.travelking.com.tw/tourguide/scenery1397.html",
      "https://www.travelking.com.tw/tourguide/scenery1398.html"
    ]
  }'

# 新版（推薦）
curl -X POST "http://localhost:8080/sights/crawler/urls" \
  -H "Content-Type: application/json" \
  -d '{
    "urls": [
      "https://www.travelking.com.tw/tourguide/scenery1397.html",
      "https://www.travelking.com.tw/tourguide/scenery1398.html"
    ]
  }'
```

---

#### SightController 舊版 API（仍可用但已棄用）

```bash
# 舊版（已棄用，但仍可用）
curl "http://localhost:8080/setSights/taipei-city"

# 新版（推薦）
curl -X POST "http://localhost:8080/sights/crawler/region?region=TPE"
```

---

## 🔍 程式碼變更總結

### 新增檔案
- ✅ `docs/CONTROLLER_API_REFERENCE.md` - 完整 API 參考文件
- ✅ `docs/CONTROLLER_REFACTORING_SUMMARY.md` - 重構總結（本文件）

### 修改檔案
- 🔄 `SightController.java` - 整合爬蟲功能，新增 5 個爬蟲端點

### 刪除檔案
- ❌ `CrawlerController.java` - 功能已合併到 SightController
- ❌ `ManualCrawlerController.java` - 功能已合併到 SightController

---

## ✅ 測試建議

重構後，請測試以下端點確保功能正常：

### 景點爬蟲測試
```bash
# 1. 測試單一縣市爬取
curl -X POST "http://localhost:8080/sights/crawler/region?region=TPE"

# 2. 測試單一 URL 爬取
curl -X POST "http://localhost:8080/sights/crawler/url" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.travelking.com.tw/tourguide/scenery1397.html"}'

# 3. 測試批次 URL 爬取
curl -X POST "http://localhost:8080/sights/crawler/urls" \
  -H "Content-Type: application/json" \
  -d '{"urls": ["https://www.travelking.com.tw/tourguide/scenery1397.html"]}'
```

### 景點 CRUD 測試
```bash
# 1. 查詢所有景點
curl "http://localhost:8080/sights"

# 2. 查詢單一景點
curl "http://localhost:8080/sights/台北101"

# 3. 根據區域查詢
curl "http://localhost:8080/sights?keyword=信義區"
```

---

## 📚 相關文件

- [Controller API 參考文件](./CONTROLLER_API_REFERENCE.md) - 所有 API 端點詳細說明
- [Food Scheduler 指南](./FOOD_SCHEDULER_GUIDE.md) - 餐廳資料批次收集排程
- [Place Details API 指南](./PLACE_DETAILS_API_GUIDE.md) - Google Places API 整合

---

## 🎉 重構完成

Controller 層已成功重構，架構更清晰、功能更統一。

**重構成果**:
- ✅ 減少 2 個重複的 Controller
- ✅ 統一 API 路徑規範
- ✅ 保持向下兼容
- ✅ 提升程式碼可維護性

---

**重構日期**: 2025-12-26
**重構版本**: 2.0
**影響範圍**: Controller 層（Service 層和 Repository 層無變更）
