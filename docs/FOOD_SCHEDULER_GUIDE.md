# 餐廳資料批次收集排程設計

## 📋 概述

本系統使用 **Spring Scheduler** 定期從 Google Places API 收集餐廳資料，並儲存到 PostgreSQL 資料庫中。

---

## 🏗️ 架構設計

### 核心組件

1. **FoodDataScheduler** - 排程任務執行器
2. **GooglePlacesService** - Google Places API 整合服務
3. **SchedulerController** - 手動觸發 API 端點

### 資料收集策略

#### 策略 1：按行政區收集（預設啟用）
- **優點**：覆蓋範圍廣，不受景點限制
- **實作**：遍歷台北市 12 個行政區
- **搜尋關鍵字**：`台北市{區名} 餐廳`
- **每區收集量**：20 筆餐廳

#### 策略 2：按景點周邊收集（可選）
- **優點**：餐廳與景點直接關聯，適合行程規劃
- **實作**：遍歷資料庫中所有有座標的景點
- **搜尋範圍**：景點周邊 500 公尺
- **每景點收集量**：10 筆餐廳

---

## ⏰ 排程設定

### 自動排程
- **執行時間**：每週日凌晨 2:00
- **Cron 表達式**：`0 0 2 * * SUN`
- **執行內容**：按行政區完整更新所有區域的餐廳資料

### 手動觸發
```bash
# 立即執行餐廳資料更新
curl -X POST http://localhost:8080/scheduler/update-foods

# 查看排程狀態
curl http://localhost:8080/scheduler/status
```

---

## 🔍 PostgreSQL 資料查詢指令

### 基本查詢

```bash
# 進入 PostgreSQL 容器
docker exec pg-local psql -U postgres -d mydb

# 或直接執行 SQL（Windows）
docker exec pg-local psql -U postgres -d mydb -c "SQL指令"
```

### 常用查詢範例

#### 1. 查看所有餐廳基本資訊
```sql
SELECT id, food_name, rating, user_ratings_total, price_range, zone, address
FROM foods
ORDER BY rating DESC;
```

#### 2. 統計資料
```sql
-- 總餐廳數、不重複的 Google Place、行政區數量
SELECT
    COUNT(*) as total_foods,
    COUNT(DISTINCT place_id) as unique_places,
    COUNT(DISTINCT zone) as zones
FROM foods;
```

#### 3. 按行政區統計
```sql
SELECT zone, COUNT(*) as count, AVG(rating) as avg_rating
FROM foods
WHERE zone IS NOT NULL
GROUP BY zone
ORDER BY count DESC;
```

#### 4. 按價格等級統計
```sql
SELECT price_range, COUNT(*) as count
FROM foods
WHERE price_range IS NOT NULL
GROUP BY price_range
ORDER BY
    CASE price_range
        WHEN '$' THEN 1
        WHEN '$$' THEN 2
        WHEN '$$$' THEN 3
        WHEN '$$$$' THEN 4
    END;
```

#### 5. 查看高評分餐廳（4.5 星以上）
```sql
SELECT food_name, rating, user_ratings_total, price_range, zone
FROM foods
WHERE rating >= 4.5
ORDER BY rating DESC, user_ratings_total DESC
LIMIT 10;
```

#### 6. 查找特定區域的餐廳
```sql
SELECT food_name, rating, address, price_range
FROM foods
WHERE zone = '信義區'
ORDER BY rating DESC;
```

#### 7. 檢查重複的餐廳（同一個 Google Place ID）
```sql
SELECT place_id, COUNT(*) as count
FROM foods
GROUP BY place_id
HAVING COUNT(*) > 1;
```

#### 8. 查看最近更新的餐廳
```sql
SELECT food_name, zone, rating, updated_at
FROM foods
ORDER BY updated_at DESC
LIMIT 10;
```

#### 9. 查看營業狀態分布
```sql
SELECT business_status, COUNT(*) as count
FROM foods
WHERE business_status IS NOT NULL
GROUP BY business_status;
```

#### 10. 查看完整餐廳資訊（包含 Google 欄位）
```sql
SELECT
    id, food_name, rating, user_ratings_total,
    price_level, price_range, business_status,
    zone, address,
    latitude, longitude,
    created_at, updated_at
FROM foods
WHERE id = 1;  -- 指定 ID
```

---

## 🚀 使用方式

### 1. 啟用排程功能

排程功能已在 `MyJavaProjectApplication.java` 中啟用：

```java
@SpringBootApplication
@EnableScheduling  // 已啟用
public class MyJavaProjectApplication {
    // ...
}
```

### 2. 測試排程（開發環境）

如果要在開發環境測試，可以修改 `FoodDataScheduler.java` 中的測試方法：

```java
// 取消註解這一行，啟用每 5 分鐘執行一次
@Scheduled(fixedRate = 300000) // 300000ms = 5分鐘
public void testSchedule() {
    // 只收集信義區測試
}
```

### 3. 手動觸發立即更新

```bash
# Windows PowerShell
curl.exe -X POST http://localhost:8080/scheduler/update-foods

# 輸出範例
{
  "success": true,
  "message": "餐廳資料更新任務已啟動，請查看日誌"
}
```

### 4. 查看執行日誌

排程執行時會在 Spring Boot 日誌中看到：

```
2025-12-26 02:00:00 INFO  - ========== 開始每週餐廳資料完整更新 ==========
2025-12-26 02:00:00 INFO  - 正在收集：台北市中正區 的餐廳資料...
2025-12-26 02:00:05 INFO  - ✅ 中正區 完成：收集 20 筆，成功儲存 18 筆
2025-12-26 02:00:07 INFO  - 正在收集：台北市大同區 的餐廳資料...
...
2025-12-26 02:05:00 INFO  - 📊 按行政區更新完成：總共儲存 215 筆餐廳資料
2025-12-26 02:05:00 INFO  - ========== 每週餐廳資料更新完成 ==========
```

---

## 📊 收集範圍規劃

### 台北市 12 行政區

| 行政區 | 預估餐廳數 | API 呼叫次數 |
|--------|-----------|-------------|
| 中正區 | 20 | 1 |
| 大同區 | 20 | 1 |
| 中山區 | 20 | 1 |
| 松山區 | 20 | 1 |
| 大安區 | 20 | 1 |
| 萬華區 | 20 | 1 |
| 信義區 | 20 | 1 |
| 士林區 | 20 | 1 |
| 北投區 | 20 | 1 |
| 內湖區 | 20 | 1 |
| 南港區 | 20 | 1 |
| 文山區 | 20 | 1 |
| **總計** | **240 筆** | **12 次** |

### Google Places API 配額管理

- **每次請求間隔**：2 秒（避免觸發限流）
- **預計總時間**：約 24 秒（12 區 × 2 秒）
- **去重機制**：根據 `place_id` 自動去重
- **更新策略**：已存在的餐廳會更新評分、評價數等資訊

---

## ⚙️ 進階配置

### 修改收集頻率

編輯 `FoodDataScheduler.java`：

```java
// 每週日凌晨 2:00（預設）
@Scheduled(cron = "0 0 2 * * SUN")

// 改為每天凌晨 3:00
@Scheduled(cron = "0 0 3 * * *")

// 改為每月 1 號凌晨 2:00
@Scheduled(cron = "0 0 2 1 * *")
```

### 修改每區收集數量

```java
// 原本：每區收集 20 筆
List<Food> foods = googlePlacesService.searchByText(query, 20);

// 改為：每區收集 50 筆
List<Food> foods = googlePlacesService.searchByText(query, 50);
```

### 切換為景點周邊收集

```java
// 在 weeklyFullUpdate() 中
// updateByZones();  // 註解掉行政區收集
updateBySights();    // 啟用景點周邊收集
```

---

## 🐛 常見問題

### Q1: 排程沒有執行？

**檢查項目**：
1. 確認 `@EnableScheduling` 已啟用
2. 確認 `FoodDataScheduler` 是 `@Component`
3. 查看 Spring Boot 啟動日誌是否有錯誤

### Q2: API 回傳 ZERO_RESULTS？

**可能原因**：
1. 搜尋關鍵字太特定
2. 該區域確實沒有餐廳資料
3. API Key 配額用盡

**解決方式**：
- 調整搜尋關鍵字（例：改用「台北 餐廳」而非「台北市XX區 餐廳」）
- 檢查 Google Cloud Console 的 API 使用量

### Q3: 重複的餐廳被多次儲存？

**不會發生**，因為：
- `Food` entity 中 `place_id` 設定為 `unique`
- `GooglePlacesService.saveOrUpdateFood()` 會先查詢是否已存在
- 如果存在，會更新而非新增

### Q4: 如何查看資料庫中的資料？

使用前面提供的 PostgreSQL 查詢指令，例如：

```bash
docker exec pg-local psql -U postgres -d mydb -c "SELECT COUNT(*) FROM foods;"
```

---

## 📝 未來擴充方向

1. **多城市支援**：擴充到新北市、桃園市等
2. **智能更新**：只更新評分/評價有變化的餐廳
3. **失敗重試**：針對失敗的 API 呼叫進行重試
4. **監控通知**：排程執行完成後發送通知（Email/Slack）
5. **資料分析**：定期生成餐廳資料統計報告
6. **增量更新**：週一到週六只更新熱門區域，週日完整更新

---

## 🔗 相關檔案

- **排程器**：`src/main/java/com/example/scheduler/FoodDataScheduler.java`
- **API 服務**：`src/main/java/com/example/service/GooglePlacesService.java`
- **手動觸發**：`src/main/java/com/example/controller/SchedulerController.java`
- **資料模型**：`src/main/java/com/example/entity/Food.java`
- **配置檔**：`src/main/resources/application.properties`

---

**最後更新**：2025-12-26
