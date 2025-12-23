# TravelKing.com.tw 爬蟲系統設計

## 目標網站分析

### TravelKing.com.tw 結構
- 主網站：https://www.travelking.com.tw/
- 旅遊導覽首頁：https://www.travelking.com.tw/tourguide/taiwan/
- 縣市景點列表：https://www.travelking.com.tw/tourguide/{county}/{city}/
  - 例：台北市 https://www.travelking.com.tw/tourguide/taipei/taipei-city/
- 景點詳細頁：https://www.travelking.com.tw/tourguide/{county}/scenery{id}.html
  - 例：八卦山 https://www.travelking.com.tw/tourguide/scenery1.html

### 網站特點
- **雙層結構**：縣市 → 行政區 → 景點列表
- **無分頁機制**：單頁完整展示所有景點
- **行政區分類**：每個縣市按行政區（如台北市12區）組織景點
- **詳細頁資訊豐富**：包含地址、經緯度、營業時間、照片、人氣指數等

## 爬蟲架構設計

### 1. 通用爬蟲基類

```java
public abstract class BaseTravelKingCrawler<T> {
    protected final String baseUrl = "https://www.travelking.com.tw";
    protected final int retryLimit = 3;
    protected final long minDelayMs = 1000;  // 最小延遲 1 秒
    protected final long maxDelayMs = 3000;  // 最大延遲 3 秒

    // 抽象方法
    protected abstract String getListUrl(String county, String city);
    protected abstract T parseDetailPage(Document doc, String url);
    protected abstract void saveItem(T item);

    // 通用方法
    public List<T> crawlByCity(String county, String city);
    public List<T> crawlByCounty(String county);
    public List<T> crawlAll();
    protected Document fetchDocument(String url);
    protected List<String> extractDetailLinks(Document listDoc);
    protected void randomDelay();
    protected String getRandomUserAgent();
}
```

### 2. 具體爬蟲實作

#### SightCrawler (景點爬蟲)
```java
@Component
public class TravelKingSightCrawler extends BaseTravelKingCrawler<Sight> {

    @Override
    protected String getListUrl(String county, String city) {
        // 例：taipei/taipei-city/
        return baseUrl + "/tourguide/" + county + "/" + city + "/";
    }

    @Override
    protected Sight parseDetailPage(Document doc, String url) {
        Sight sight = new Sight();

        // 1. 景點名稱 (從 <h1> 標籤)
        sight.setSightName(doc.select("h1").first().text());

        // 2. 基本資訊 (從 #point_data 區塊)
        Element pointData = doc.select("#point_data").first();
        sight.setAddress(pointData.select("地址").text());
        sight.setCategory(pointData.select("景點類別").text());

        // 3. 景點描述 (多段落文字)
        String description = doc.select(".description").text();
        sight.setDescription(description);

        // 4. 照片 URLs (從 Swiper 輪播)
        String[] photoUrls = doc.select(".swiper-slide img")
            .stream()
            .map(img -> img.attr("src"))
            .toArray(String[]::new);
        sight.setPhotoUrls(photoUrls);

        // 5. 營業時間
        String openingHours = doc.select(".business-hours").text();
        sight.setOpeningHours(openingHours);

        // 6. 經緯度 (從 Google Maps 連結解析)
        String mapLink = doc.select("a[href*=google.com/maps]").attr("href");
        parseLatLng(mapLink, sight);

        // 7. 人氣指數
        String popularity = doc.select(".popularity").text();
        sight.setViewCount(Integer.parseInt(popularity));

        return sight;
    }

    private void parseLatLng(String mapLink, Sight sight) {
        // 從 URL 解析經緯度：24.0770862685263,120.549057732064
        Pattern pattern = Pattern.compile("(\\d+\\.\\d+),(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(mapLink);
        if (matcher.find()) {
            sight.setLatitude(new BigDecimal(matcher.group(1)));
            sight.setLongitude(new BigDecimal(matcher.group(2)));
        }
    }
}
```

#### 縣市城市映射表
```java
@Component
public class CityMappingConfig {
    // TravelKing 使用的縣市/城市 URL slug
    public static final Map<String, List<String>> COUNTY_CITY_MAP = Map.ofEntries(
        Map.entry("taipei", List.of("taipei-city")),  // 台北市
        Map.entry("newtaipei", List.of("...各區...")), // 新北市（需要根據實際網站確認）
        Map.entry("taichung", List.of("...")),        // 台中市
        Map.entry("tainan", List.of("...")),          // 台南市
        Map.entry("kaohsiung", List.of("...")),       // 高雄市
        // ... 其他縣市
    );
}
```

## 爬蟲策略

### 1. 分階段爬取

**Phase 1: 列表頁爬取（快速）**
- 爬取所有縣市的列表頁
- 取得基本資訊（名稱、地址、縮圖）
- 儲存概要資料

**Phase 2: 詳細頁爬取（深度）**
- 根據列表頁的連結
- 爬取每個項目的詳細頁面
- 補充完整資訊

**Phase 3: 增量更新**
- 定期更新已存在的資料
- 只爬取新增或變更的項目

### 2. 爬蟲控制

```java
@Service
public class CrawlerScheduler {

    @Scheduled(cron = "0 0 2 * * SUN")  // 每週日凌晨 2 點
    public void weeklyFullCrawl() {
        // 完整爬取所有資料
    }

    @Scheduled(cron = "0 0 */6 * * *")  // 每 6 小時
    public void incrementalUpdate() {
        // 增量更新
    }

    public CrawlStatus crawlOnDemand(CrawlRequest request) {
        // 手動觸發爬取
    }
}
```

### 3. 反爬蟲對策

- **延遲請求**：每個請求間隔 1-3 秒（隨機）
- **User-Agent 輪換**：模擬不同瀏覽器
- **代理 IP**：如果需要（選配）
- **請求重試**：失敗時重試 3 次，指數退避
- **尊重 robots.txt**：檢查爬蟲規則

```java
public class CrawlerConfig {
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)...",
        "Mozilla/5.0 (X11; Linux x86_64)..."
    };

    public String getRandomUserAgent() {
        return USER_AGENTS[new Random().nextInt(USER_AGENTS.length)];
    }
}
```

## 資料處理流程

```
爬取 → 解析 → 驗證 → 清洗 → 儲存
  ↓      ↓      ↓      ↓      ↓
URL   HTML   Data   Clean   DB
      ↓
    Cache（避免重複爬取）
```

### 資料清洗

```java
public class DataCleaner {

    public String cleanAddress(String rawAddress) {
        // 移除多餘空白、統一格式
    }

    public String cleanPhone(String rawPhone) {
        // 統一電話格式：(02)2345-6789
    }

    public List<String> extractTags(String description) {
        // 從描述中提取標籤
        // 使用 NLP 或關鍵字比對
    }

    public LatLng geocodeAddress(String address) {
        // 地址轉經緯度（使用 Google Geocoding API）
    }
}
```

## 錯誤處理

### 1. 爬蟲錯誤記錄

```java
@Entity
public class CrawlLog {
    private Long id;
    private String crawlerType;    // sight, food, festival
    private String url;
    private String status;         // success, failed, partial
    private String errorMessage;
    private Integer itemsProcessed;
    private LocalDateTime crawlTime;
}
```

### 2. 失敗重試機制

```java
@Retryable(
    value = {IOException.class, HttpClientErrorException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000, multiplier = 2)
)
public Document fetchWithRetry(String url) {
    // 帶重試的爬取
}
```

## Controller API 設計

```java
@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    @PostMapping("/sights/crawl")
    public CrawlResponse crawlSights(@RequestParam String region) {
        // 爬取指定縣市的景點
    }

    @PostMapping("/foods/crawl")
    public CrawlResponse crawlFoods(@RequestParam String region) {
        // 爬取指定縣市的美食
    }

    @PostMapping("/festivals/crawl")
    public CrawlResponse crawlFestivals(@RequestParam String region) {
        // 爬取指定縣市的節慶
    }

    @PostMapping("/all/crawl")
    public CrawlResponse crawlAll() {
        // 爬取所有縣市的所有資料
    }

    @GetMapping("/status")
    public CrawlStatus getStatus() {
        // 查詢爬蟲執行狀態
    }

    @GetMapping("/logs")
    public Page<CrawlLog> getLogs(Pageable pageable) {
        // 查詢爬蟲日誌
    }
}
```

## 效能優化

### 1. 批次處理

```java
public void batchSave(List<Sight> sights) {
    int batchSize = 100;
    for (int i = 0; i < sights.size(); i += batchSize) {
        List<Sight> batch = sights.subList(i,
            Math.min(i + batchSize, sights.size()));
        sightRepository.saveAll(batch);
        sightRepository.flush();
    }
}
```

### 2. 並行爬取

```java
@Async
public CompletableFuture<List<Sight>> crawlRegionAsync(String region) {
    // 非同步爬取單一縣市
}

public void crawlAllRegionsParallel() {
    List<CompletableFuture<List<Sight>>> futures =
        REGIONS.stream()
            .map(this::crawlRegionAsync)
            .collect(Collectors.toList());

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .join();
}
```

### 3. 快取機制

```java
@Cacheable(value = "crawl-cache", key = "#url")
public Document getCachedDocument(String url) {
    // 快取已爬取的頁面（24小時）
}
```

## 監控與通知

### 1. 爬蟲進度追蹤

```java
@Component
public class CrawlProgressTracker {

    private final Map<String, CrawlProgress> progressMap =
        new ConcurrentHashMap<>();

    public void updateProgress(String taskId, int current, int total) {
        // 更新進度
    }

    public CrawlProgress getProgress(String taskId) {
        // 查詢進度
    }
}
```

### 2. 完成通知（選配）

```java
@Service
public class CrawlerNotificationService {

    public void sendCompletionEmail(CrawlSummary summary) {
        // 爬蟲完成後發送郵件通知
    }

    public void sendErrorAlert(CrawlError error) {
        // 爬蟲錯誤時發送警報
    }
}
```

## 縣市代碼對照表

```java
public enum TaiwanRegion {
    TAIPEI("TPE", "台北市"),
    NEW_TAIPEI("TPH", "新北市"),
    TAOYUAN("TAO", "桃園市"),
    TAICHUNG("TXG", "台中市"),
    TAINAN("TNN", "台南市"),
    KAOHSIUNG("KHH", "高雄市"),
    KEELUNG("KEL", "基隆市"),
    HSINCHU_CITY("HSZ", "新竹市"),
    CHIAYI_CITY("CYI", "嘉義市"),
    HSINCHU_COUNTY("HSQ", "新竹縣"),
    MIAOLI("MIA", "苗栗縣"),
    CHANGHUA("CHA", "彰化縣"),
    NANTOU("NAN", "南投縣"),
    YUNLIN("YUN", "雲林縣"),
    CHIAYI_COUNTY("CYQ", "嘉義縣"),
    PINGTUNG("PIF", "屏東縣"),
    YILAN("ILA", "宜蘭縣"),
    HUALIEN("HUA", "花蓮縣"),
    TAITUNG("TTT", "台東縣"),
    PENGHU("PEN", "澎湖縣"),
    KINMEN("KIN", "金門縣"),
    LIENCHIANG("LIE", "連江縣");

    private final String code;
    private final String name;
}
```

## 測試策略

### 1. 單元測試

```java
@Test
public void testParseSight() {
    String html = loadTestHtml("sight_sample.html");
    Sight sight = crawler.parseItem(Jsoup.parse(html));

    assertNotNull(sight.getSightName());
    assertNotNull(sight.getAddress());
}
```

### 2. 整合測試

```java
@Test
@Disabled("需要實際網路連線")
public void testCrawlRealWebsite() {
    List<Sight> sights = crawler.crawlByRegion("TPE");
    assertTrue(sights.size() > 0);
}
```

## 注意事項

1. **遵守網站使用條款** - 檢查 taiwan.net.tw 的服務條款
2. **合理的爬取頻率** - 不要對伺服器造成負擔
3. **資料使用授權** - 確認資料使用的法律問題
4. **持續維護** - 網站改版時需要更新爬蟲邏輯
5. **資料品質** - 定期檢查爬取資料的正確性

## 預估爬取時間

假設：
- 每個縣市約 50-200 個景點
- 每個請求延遲 2 秒
- 22 個縣市

**列表頁爬取：** 約 1-2 分鐘
**詳細頁爬取：** 約 2-4 小時（全台約 2000+ 個項目）
**完整爬取（景點+美食+節慶）：** 約 6-12 小時

建議：首次爬取分批進行，後續使用排程增量更新。
