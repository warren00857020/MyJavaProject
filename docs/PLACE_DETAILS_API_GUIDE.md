# Google Place Details API 整合指南

## 📋 問題背景

### 目前的問題
使用 **Text Search API** 和 **Nearby Search API** 只能獲得簡化的營業時間資訊：

```json
{
  "opening_hours": {
    "open_now": true  // ❌ 只有當前是否營業
  }
}
```

### 解決方案
使用 **Place Details API** 可以獲取完整的營業時間資訊：

```json
{
  "opening_hours": {
    "open_now": true,
    "periods": [
      {
        "close": { "day": 0, "time": "2100" },
        "open": { "day": 0, "time": "1100" }
      }
    ],
    "weekday_text": [
      "星期一: 11:00 – 21:00",
      "星期二: 11:00 – 21:00",
      "星期三: 11:00 – 21:00",
      "星期四: 11:00 – 21:00",
      "星期五: 11:00 – 21:00",
      "星期六: 11:00 – 22:00",
      "星期日: 11:00 – 22:00"
    ]
  },
  "formatted_phone_number": "02 2723 4567",
  "website": "https://example.com"
}
```

---

## 🔧 實作說明

### 新增的功能

#### 1. **獲取單一餐廳詳細資訊**
```java
public Food getPlaceDetails(String placeId)
```

**API 欄位**：
- `name` - 餐廳名稱
- `rating` - 評分
- `user_ratings_total` - 評價數
- `price_level` - 價格等級
- `formatted_address` - 完整地址
- `geometry` - 座標
- `formatted_phone_number` - 格式化電話號碼
- `website` - 官方網站
- `opening_hours` - **完整營業時間**（包含 periods 和 weekday_text）
- `business_status` - 營業狀態
- `types` - 類型陣列
- `photos` - 照片參考

#### 2. **補充餐廳詳細資訊**
```java
public Food enrichWithDetails(Food food)
```

為現有餐廳物件補充：
- 完整營業時間
- 電話號碼
- 官方網站

---

## 🚀 使用方式

### 方法 1：獲取單一餐廳詳細資訊

```bash
# 獲取 ID=1 的餐廳完整資訊（包含營業時間）
curl http://localhost:8080/foods/1/details
```

**回應範例**：
```json
{
  "success": true,
  "message": "已更新完整營業時間、電話和網站資訊",
  "food": {
    "id": 1,
    "foodName": "默爾 pasta pizza(台北信義威秀店)",
    "placeId": "ChIJpzAkec2rQjQRLYceVmNNtq4",
    "rating": 4.7,
    "userRatingsTotal": 2106,
    "phone": "02 2723 1234",
    "officialWebsite": "https://www.morpasta.com.tw",
    "openingHours": "{\"open_now\":true,\"periods\":[{\"close\":{\"day\":0,\"time\":\"2100\"},\"open\":{\"day\":0,\"time\":\"1100\"}}],\"weekday_text\":[\"星期一: 11:00 – 21:00\",\"星期二: 11:00 – 21:00\",\"星期三: 11:00 – 21:00\",\"星期四: 11:00 – 21:00\",\"星期五: 11:00 – 21:00\",\"星期六: 11:00 – 22:00\",\"星期日: 11:00 – 22:00\"]}"
  }
}
```

### 方法 2：批次補充所有餐廳詳細資訊

```bash
# 補充前 10 筆餐廳的詳細資訊
curl -X POST "http://localhost:8080/foods/enrich-all?limit=10"
```

**回應範例**：
```json
{
  "success": true,
  "total": 7,
  "processed": 7,
  "success_count": 7,
  "failed_count": 0,
  "message": "已處理 7/7 筆餐廳資料"
}
```

**注意事項**：
- 每個餐廳會呼叫一次 Place Details API
- 預設限制為 10 筆，避免消耗太多配額
- 每次呼叫間隔 1 秒，防止觸發限流
- 7 筆餐廳約需 7 秒完成

---

## 📊 營業時間資料結構

### JSON 格式說明

```json
{
  "open_now": true,  // 當前是否營業
  "periods": [       // 營業時段（機器可讀）
    {
      "close": {
        "day": 0,    // 星期日 (0=週日, 1=週一, ..., 6=週六)
        "time": "2100"  // 21:00 關門
      },
      "open": {
        "day": 0,
        "time": "1100"  // 11:00 開門
      }
    },
    {
      "close": { "day": 1, "time": "2100" },
      "open": { "day": 1, "time": "1100" }
    }
    // ... 其他日期
  ],
  "weekday_text": [  // 營業時間文字（人類可讀）
    "星期一: 11:00 – 21:00",
    "星期二: 11:00 – 21:00",
    "星期三: 11:00 – 21:00",
    "星期四: 11:00 – 21:00",
    "星期五: 11:00 – 21:00",
    "星期六: 11:00 – 22:00",
    "星期日: 11:00 – 22:00"
  ]
}
```

### 在資料庫中的儲存

- **欄位**：`opening_hours`
- **類型**：`TEXT`
- **內容**：完整的 JSON 字串

---

## 🔍 查看資料庫中的完整營業時間

### SQL 查詢

```sql
-- 查看完整營業時間 JSON
SELECT id, food_name, opening_hours
FROM foods
WHERE id = 1;
```

### 提取特定欄位（PostgreSQL）

```sql
-- 提取 open_now 狀態
SELECT
    food_name,
    opening_hours::json->'open_now' as is_open_now
FROM foods
WHERE opening_hours IS NOT NULL;

-- 提取 weekday_text 陣列
SELECT
    food_name,
    opening_hours::json->'weekday_text' as weekly_schedule
FROM foods
WHERE opening_hours IS NOT NULL;
```

---

## 💡 最佳實踐建議

### 1. **兩階段收集策略**

**階段 1：快速收集基本資料**
- 使用 Text Search API / Nearby Search API
- 快速獲取大量餐廳的基本資訊（名稱、評分、地址）
- 不消耗 Place Details API 配額

**階段 2：補充詳細資料**
- 針對需要的餐廳呼叫 Place Details API
- 獲取完整營業時間、電話、網站
- 分批處理，控制配額使用

### 2. **實作建議**

```java
// ❌ 不推薦：在搜尋時立即獲取所有詳細資訊
List<Food> foods = searchByText("台北市信義區 餐廳", 20);
for (Food food : foods) {
    enrichWithDetails(food);  // 20 次 Place Details API 呼叫
}

// ✅ 推薦：先儲存基本資訊，後續按需補充
List<Food> foods = searchByText("台北市信義區 餐廳", 20);
saveOrUpdateBatch(foods);  // 只使用 Text Search API

// 之後只對熱門或需要的餐廳補充詳細資訊
Food topRated = foods.get(0);
enrichWithDetails(topRated);  // 1 次 Place Details API 呼叫
```

### 3. **前端展示建議**

```javascript
// 前端可以解析 JSON 展示營業時間
const openingHours = JSON.parse(food.openingHours);

if (openingHours.weekday_text) {
  openingHours.weekday_text.forEach(day => {
    console.log(day);  // 星期一: 11:00 – 21:00
  });
}

// 顯示當前是否營業
if (openingHours.open_now) {
  console.log("營業中 ✅");
} else {
  console.log("休息中 ⛔");
}
```

---

## 📈 API 配額管理

### Google Places API 免費額度

- **Text Search API**: 免費 0 次/月（需付費）
- **Nearby Search API**: 免費 0 次/月（需付費）
- **Place Details API**: 免費 0 次/月（需付費）

### 實際配額（依你的方案）

請到 [Google Cloud Console](https://console.cloud.google.com/apis/dashboard) 查看：
- APIs & Services > Dashboard
- 選擇 Places API
- 查看 Quotas

### 節省配額的策略

1. **快取策略**：
   - 已獲取過詳細資訊的餐廳不重複呼叫
   - 設定快取有效期（例：30 天）

2. **按需獲取**：
   - 只在使用者點擊「查看詳情」時呼叫 Place Details API
   - 不預先載入所有餐廳的詳細資訊

3. **批次控制**：
   - 使用 `limit` 參數限制批次處理數量
   - 分多次執行，避免一次性消耗太多配額

---

## 🧪 測試範例

### 測試單一餐廳

```bash
# 步驟 1：查看目前的 opening_hours（簡化版）
docker exec pg-local psql -U postgres -d mydb -c "SELECT id, food_name, opening_hours FROM foods WHERE id = 1;"

# 步驟 2：補充詳細資訊
curl http://localhost:8080/foods/1/details

# 步驟 3：再次查看（完整版）
docker exec pg-local psql -U postgres -d mydb -c "SELECT id, food_name, opening_hours FROM foods WHERE id = 1;"
```

### 測試批次補充

```bash
# 補充前 3 筆餐廳
curl -X POST "http://localhost:8080/foods/enrich-all?limit=3"

# 查看結果
docker exec pg-local psql -U postgres -d mydb -c "SELECT id, food_name, phone, official_website, SUBSTRING(opening_hours, 1, 100) as hours_preview FROM foods LIMIT 3;"
```

---

## 🔗 相關檔案

- **服務層**：`src/main/java/com/example/service/GooglePlacesService.java`
  - `getPlaceDetails()` - 獲取詳細資訊
  - `enrichWithDetails()` - 補充詳細資訊

- **控制器**：`src/main/java/com/example/controller/FoodController.java`
  - `GET /foods/{id}/details` - 單一餐廳詳情
  - `POST /foods/enrich-all` - 批次補充

- **資料模型**：`src/main/java/com/example/entity/Food.java`
  - `opening_hours` (TEXT) - 完整營業時間 JSON

---

## ❓ 常見問題

### Q1: 為什麼要分兩階段收集？

**答**：
- Text Search API 一次可獲取 20 筆餐廳基本資訊（1 次 API 呼叫）
- 如果每筆都呼叫 Place Details API，需要 20 次 API 呼叫
- 分兩階段可節省 95% 的配額（20 vs 1）

### Q2: opening_hours 為什麼用 TEXT 而不是 JSONB？

**答**：
- JSONB 需要 PostgreSQL 特殊語法，且 JPA 映射複雜
- TEXT 儲存 JSON 字串更簡單，前端解析即可
- 如果需要在資料庫層面查詢營業時間，可改用 JSONB

### Q3: 如何判斷餐廳現在是否營業？

**答**：
```java
// 後端解析
JSONObject hours = new JSONObject(food.getOpeningHours());
boolean isOpen = hours.getBoolean("open_now");

// 或前端解析
const hours = JSON.parse(food.openingHours);
const isOpen = hours.open_now;
```

### Q4: 如果某些餐廳沒有營業時間資料怎麼辦？

**答**：
- Google Places API 可能不提供某些餐廳的營業時間
- 程式會檢查 `result.has("opening_hours")` 避免錯誤
- 資料庫中 `opening_hours` 欄位允許 NULL

---

**最後更新**：2025-12-26
**API 版本**：Google Places API (New)
