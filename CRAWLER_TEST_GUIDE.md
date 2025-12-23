# 爬蟲系統測試指南

## 系統架構

已完成的爬蟲系統包含以下組件：

### 核心類別
- **BaseTravelKingCrawler** - 通用爬蟲基類
- **TravelKingSightCrawler** - 景點爬蟲實作
- **CrawlerService** - 爬蟲服務層
- **CrawlerController** - REST API 端點

### 配置類別
- **CrawlerConfig** - 爬蟲配置（延遲、User-Agent 等）
- **CityMappingConfig** - 縣市代碼映射

### 工具類別
- **DataCleaner** - 資料清洗工具

## 準備工作

### 1. 確保資料庫已初始化

```bash
# 確認 PostgreSQL 正在運行
psql -U postgres -d mydb -c "SELECT * FROM regions LIMIT 5;"
```

確保 `regions` 表已有 22 個縣市資料。如果沒有，執行：

```bash
psql -U postgres -d mydb -f src/main/resources/data-init.sql
```

### 2. 啟動應用程式

```bash
# 使用 Maven Wrapper
./mvnw spring-boot:run

# 或直接執行 JAR
./mvnw clean package
java -jar target/myJavaProject-0.0.1-SNAPSHOT.jar
```

## 測試方法

### 方法 1: 使用 REST API 測試

#### 1.1 健康檢查

```bash
curl http://localhost:8080/api/crawler/health
```

預期輸出：
```json
{
  "status": "ok",
  "service": "crawler",
  "message": "爬蟲服務正常運行"
}
```

#### 1.2 爬取單一縣市（台北市）

```bash
curl "http://localhost:8080/api/crawler/sights?region=TPE"
```

預期輸出：
```json
{
  "regionCode": "TPE",
  "type": "sight",
  "success": true,
  "itemsProcessed": 50,
  "message": "成功爬取 50 個景點",
  "startTime": 1234567890,
  "endTime": 1234567900,
  "duration": 10000
}
```

#### 1.3 爬取多個縣市（批次）

```bash
curl -X POST http://localhost:8080/api/crawler/sights/batch \
  -H "Content-Type: application/json" \
  -d '{"regions": ["TPE", "TXG", "KHH"]}'
```

#### 1.4 爬取所有縣市（⚠️ 耗時較長）

```bash
curl -X POST http://localhost:8080/api/crawler/sights/all
```

**注意**：這會爬取全台 22 個縣市，預計需要 2-6 小時。

### 方法 2: 在應用程式中測試

可以建立一個測試端點或使用 Spring Boot Test。

## 驗證結果

### 1. 檢查資料庫

```sql
-- 查看爬取的景點數量
SELECT COUNT(*) FROM sights;

-- 查看各縣市的景點數量
SELECT r.name, COUNT(s.id) as sight_count
FROM regions r
LEFT JOIN sights s ON r.id = s.region_id
GROUP BY r.name
ORDER BY sight_count DESC;

-- 查看最近爬取的景點
SELECT sight_name, address, category, created_at
FROM sights
ORDER BY created_at DESC
LIMIT 10;

-- 檢查是否有照片 URL
SELECT sight_name, photo_urls
FROM sights
WHERE photo_urls IS NOT NULL AND array_length(photo_urls, 1) > 0
LIMIT 5;

-- 檢查經緯度資料
SELECT sight_name, latitude, longitude
FROM sights
WHERE latitude IS NOT NULL AND longitude IS NOT NULL
LIMIT 10;
```

### 2. 使用 API 查詢爬取的資料

```bash
# 查詢所有景點
curl http://localhost:8080/api/sights

# 查詢台北市的景點
curl http://localhost:8080/api/sights/region/1

# 搜尋關鍵字
curl "http://localhost:8080/api/sights/search?keyword=101"
```

## 預期結果

### 成功指標
- ✅ 每個縣市至少爬取到 10+ 個景點
- ✅ 景點名稱、地址、描述都有資料
- ✅ 至少 50% 的景點有照片 URL
- ✅ 至少 30% 的景點有經緯度資料
- ✅ 沒有重複的景點（檢查 sight_name）

### 資料完整性檢查

```sql
-- 檢查必填欄位是否有空值
SELECT
  COUNT(*) as total,
  COUNT(sight_name) as has_name,
  COUNT(address) as has_address,
  COUNT(description) as has_description,
  COUNT(category) as has_category
FROM sights;

-- 檢查選填欄位的填充率
SELECT
  COUNT(*) as total,
  COUNT(phone) as has_phone,
  COUNT(opening_hours) as has_opening_hours,
  COUNT(latitude) as has_coordinates,
  SUM(CASE WHEN photo_urls IS NOT NULL AND array_length(photo_urls, 1) > 0 THEN 1 ELSE 0 END) as has_photos
FROM sights;
```

## 常見問題

### 問題 1: 爬取失敗，顯示連線錯誤

**原因**: 網站可能封鎖了請求，或網路連線有問題。

**解決方法**:
- 檢查網路連線
- 增加延遲時間（修改 `CrawlerConfig.MIN_DELAY_MS`）
- 檢查 TravelKing 網站是否可正常訪問

### 問題 2: 景點資料不完整

**原因**: 網站 HTML 結構可能與預期不同。

**解決方法**:
1. 手動訪問一個景點詳細頁，例如：
   https://www.travelking.com.tw/tourguide/scenery1.html

2. 檢查實際的 HTML 結構

3. 修改 `TravelKingSightCrawler.parseDetailPage()` 中的選擇器

### 問題 3: 找不到縣市（Region not found）

**原因**: `regions` 表尚未初始化。

**解決方法**:
```bash
psql -U postgres -d mydb -f src/main/resources/data-init.sql
```

### 問題 4: 爬取速度太慢

**原因**: 為了避免被封鎖，設定了 1-3 秒的隨機延遲。

**調整方法**（⚠️ 風險：可能被封鎖）:
```java
// 在 CrawlerConfig.java 中調整
public static final long MIN_DELAY_MS = 500;   // 減少到 0.5 秒
public static final long MAX_DELAY_MS = 1500;  // 減少到 1.5 秒
```

### 問題 5: OutOfMemoryError

**原因**: 一次爬取太多資料。

**解決方法**:
- 使用批次爬取而非全部爬取
- 增加 JVM 記憶體：`java -Xmx2g -jar ...`

## 下一步

完成景點爬蟲測試後，可以：

1. **實作美食爬蟲** (`TravelKingFoodCrawler`)
2. **實作節慶爬蟲** (`TravelKingFestivalCrawler`)
3. **增加排程功能** - 自動定期更新資料
4. **優化爬蟲效能** - 使用多執行緒、快取機制
5. **增加錯誤日誌** - 記錄爬取失敗的 URL

## 爬蟲倫理

請遵守以下準則：
- ✅ 尊重網站的 `robots.txt`
- ✅ 設定合理的延遲時間（1-3 秒）
- ✅ 不要對伺服器造成負擔
- ✅ 僅用於學習和合法用途
- ❌ 不要用於商業目的（需確認網站授權）
- ❌ 不要繞過反爬蟲機制

## 調試技巧

### 開啟詳細日誌

在 `application.properties` 中加入：

```properties
# 爬蟲日誌
logging.level.com.example.crawler=DEBUG

# Jsoup 日誌
logging.level.org.jsoup=DEBUG
```

### 測試單一景點解析

可以建立一個簡單的測試類：

```java
@SpringBootTest
public class CrawlerTest {

    @Autowired
    private TravelKingSightCrawler crawler;

    @Test
    public void testParseSingleSight() throws IOException {
        String url = "https://www.travelking.com.tw/tourguide/scenery1.html";
        Document doc = Jsoup.connect(url).get();
        Sight sight = crawler.parseDetailPage(doc, url);

        System.out.println("景點名稱: " + sight.getSightName());
        System.out.println("地址: " + sight.getAddress());
        System.out.println("描述: " + sight.getDescription());

        assertNotNull(sight.getSightName());
    }
}
```

## 監控建議

建議監控以下指標：
- 爬取成功率
- 平均爬取時間
- 資料完整性
- 重複資料數量
- 錯誤日誌

可以考慮整合：
- Prometheus + Grafana（監控）
- ELK Stack（日誌分析）
