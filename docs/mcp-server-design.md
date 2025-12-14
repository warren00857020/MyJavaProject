# Taiwan Travel MCP Server 設計文件

## 概述

這是一個基於 Model Context Protocol (MCP) 的智慧旅遊助手，讓使用者可以用自然語言與 AI 互動，自動規劃台灣旅遊行程。

## MCP 協議基礎

### 什麼是 MCP？

Model Context Protocol (MCP) 是一個開放協議，讓 AI 模型能夠安全地存取外部資料和工具。

### 核心概念

1. **Tools** - AI 可以呼叫的函式（如：搜尋景點、規劃路線）
2. **Resources** - AI 可以讀取的資料（如：景點列表、美食資訊）
3. **Prompts** - 預設的提示詞模板（如：一日遊規劃師）

### 通訊協議

- **Transport**: Server-Sent Events (SSE) 或 stdio
- **Message Format**: JSON-RPC 2.0
- **Encoding**: UTF-8

## 系統架構

```
┌─────────────────────────────────────────────────────────┐
│                  Claude / AI Assistant                   │
│              (透過 MCP SDK 連接 MCP Server)              │
└────────────────────┬────────────────────────────────────┘
                     │ JSON-RPC 2.0 over SSE/stdio
                     ▼
┌─────────────────────────────────────────────────────────┐
│              Taiwan Travel MCP Server                    │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   Server    │  │   Request    │  │   Response   │   │
│  │   Manager   │  │   Handler    │  │   Builder    │   │
│  └─────────────┘  └──────────────┘  └──────────────┘   │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Tool Registry                        │  │
│  │  - search_sights                                 │  │
│  │  - search_foods                                  │  │
│  │  - search_festivals                              │  │
│  │  - plan_itinerary                                │  │
│  │  - calculate_route                               │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │           Resource Provider                       │  │
│  │  - taiwan://sights/{region}                      │  │
│  │  - taiwan://foods/{region}                       │  │
│  │  - taiwan://festivals/{month}                    │  │
│  └──────────────────────────────────────────────────┘  │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │            Prompt Templates                       │  │
│  │  - family-trip-planner                           │  │
│  │  - food-tour-guide                               │  │
│  │  - cultural-experience                           │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│           Spring Boot Backend Services                   │
│  SightService | FoodService | FestivalService | etc.    │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  PostgreSQL Database                     │
└─────────────────────────────────────────────────────────┘
```

## 核心組件設計

### 1. MCP Server 主程式

```java
@Component
public class TaiwanTravelMcpServer implements McpServer {

    private final ToolRegistry toolRegistry;
    private final ResourceProvider resourceProvider;
    private final PromptRegistry promptRegistry;

    @Override
    public void start() {
        // 啟動 MCP Server (SSE endpoint)
        // 監聽在 /mcp/sse
    }

    @Override
    public McpResponse handleRequest(McpRequest request) {
        switch (request.getMethod()) {
            case "tools/list":
                return listTools();
            case "tools/call":
                return callTool(request);
            case "resources/list":
                return listResources();
            case "resources/read":
                return readResource(request);
            case "prompts/list":
                return listPrompts();
            case "prompts/get":
                return getPrompt(request);
            default:
                throw new McpException("Unknown method");
        }
    }
}
```

### 2. Tools 定義

#### Tool 1: search_sights

```java
@McpTool(
    name = "search_sights",
    description = "搜尋台灣景點，支援依區域、類別、關鍵字篩選"
)
public class SearchSightsTool implements Tool {

    @ToolParameter(
        name = "region",
        description = "縣市名稱（如：台北市、台南市）",
        required = false
    )
    private String region;

    @ToolParameter(
        name = "category",
        description = "景點類別（如：古蹟、自然景觀、博物館）",
        required = false
    )
    private String category;

    @ToolParameter(
        name = "keyword",
        description = "關鍵字搜尋",
        required = false
    )
    private String keyword;

    @ToolParameter(
        name = "tags",
        description = "標籤篩選（如：親子、網美、歷史）",
        required = false
    )
    private List<String> tags;

    @Override
    public ToolResult execute() {
        List<Sight> sights = sightService.search(region, category, keyword, tags);
        return ToolResult.success(sights);
    }
}
```

#### Tool 2: search_foods

```java
@McpTool(
    name = "search_foods",
    description = "搜尋台灣美食，包含餐廳、小吃、夜市等"
)
public class SearchFoodsTool implements Tool {

    @ToolParameter(name = "region", description = "縣市名稱")
    private String region;

    @ToolParameter(name = "cuisine_type", description = "菜系（如：台菜、客家菜、原住民料理）")
    private String cuisineType;

    @ToolParameter(name = "category", description = "分類（如：小吃、餐廳、甜點）")
    private String category;

    @ToolParameter(name = "price_range", description = "價格範圍（$, $$, $$$）")
    private String priceRange;

    @Override
    public ToolResult execute() {
        List<Food> foods = foodService.search(region, cuisineType, category, priceRange);
        return ToolResult.success(foods);
    }
}
```

#### Tool 3: search_festivals

```java
@McpTool(
    name = "search_festivals",
    description = "搜尋台灣傳統節慶與活動"
)
public class SearchFestivalsTool implements Tool {

    @ToolParameter(name = "region", description = "縣市名稱")
    private String region;

    @ToolParameter(name = "month", description = "月份（1-12）")
    private Integer month;

    @ToolParameter(name = "category", description = "類別（如：傳統節慶、音樂祭、藝術展）")
    private String category;

    @Override
    public ToolResult execute() {
        List<Festival> festivals = festivalService.search(region, month, category);
        return ToolResult.success(festivals);
    }
}
```

#### Tool 4: plan_itinerary (最重要)

```java
@McpTool(
    name = "plan_itinerary",
    description = "智慧規劃旅遊行程，根據使用者需求自動安排景點、美食、活動"
)
public class PlanItineraryTool implements Tool {

    @ToolParameter(name = "regions", description = "想去的縣市（可多個）", required = true)
    private List<String> regions;

    @ToolParameter(name = "days", description = "旅遊天數", required = true)
    private Integer days;

    @ToolParameter(name = "travel_style", description = "旅遊風格（美食、文化、自然、親子等）")
    private String travelStyle;

    @ToolParameter(name = "budget", description = "預算（新台幣）")
    private BigDecimal budget;

    @ToolParameter(name = "preferences", description = "偏好（如：避免爬山、喜歡海邊、素食）")
    private List<String> preferences;

    @ToolParameter(name = "start_date", description = "出發日期（yyyy-MM-dd）")
    private LocalDate startDate;

    @Override
    public ToolResult execute() {
        // 複雜的行程規劃邏輯
        ItineraryPlan plan = itineraryService.planItinerary(
            regions, days, travelStyle, budget, preferences, startDate
        );
        return ToolResult.success(plan);
    }
}
```

#### Tool 5: calculate_route

```java
@McpTool(
    name = "calculate_route",
    description = "計算多個地點之間的最佳路線與交通方式"
)
public class CalculateRouteTool implements Tool {

    @ToolParameter(name = "locations", description = "地點列表（景點名稱或地址）", required = true)
    private List<String> locations;

    @ToolParameter(name = "transport_mode", description = "交通方式（driving, transit, walking）")
    private String transportMode;

    @Override
    public ToolResult execute() {
        RouteInfo route = routeService.calculateOptimalRoute(locations, transportMode);
        return ToolResult.success(route);
    }
}
```

#### Tool 6: get_recommendations

```java
@McpTool(
    name = "get_recommendations",
    description = "基於使用者偏好和歷史資料，推薦景點或美食"
)
public class GetRecommendationsTool implements Tool {

    @ToolParameter(name = "type", description = "推薦類型（sight, food, festival）", required = true)
    private String type;

    @ToolParameter(name = "region", description = "縣市名稱")
    private String region;

    @ToolParameter(name = "current_location", description = "當前位置（地址或景點名稱）")
    private String currentLocation;

    @ToolParameter(name = "limit", description = "回傳數量")
    private Integer limit;

    @Override
    public ToolResult execute() {
        List<Recommendation> recommendations =
            recommendationService.getRecommendations(type, region, currentLocation, limit);
        return ToolResult.success(recommendations);
    }
}
```

### 3. Resources 定義

Resources 提供唯讀的資料存取，使用 URI scheme。

```java
@Component
public class TaiwanResourceProvider implements ResourceProvider {

    @Override
    public List<Resource> listResources() {
        return List.of(
            Resource.of("taiwan://sights/all", "所有景點列表"),
            Resource.of("taiwan://sights/{region}", "特定縣市的景點"),
            Resource.of("taiwan://foods/{region}", "特定縣市的美食"),
            Resource.of("taiwan://festivals/{month}", "特定月份的節慶"),
            Resource.of("taiwan://categories", "所有景點分類"),
            Resource.of("taiwan://regions", "台灣縣市列表")
        );
    }

    @Override
    public ResourceContent readResource(String uri) {
        UriParser parser = new UriParser(uri);

        switch (parser.getScheme()) {
            case "taiwan":
                return handleTaiwanResource(parser);
            default:
                throw new ResourceNotFoundException(uri);
        }
    }

    private ResourceContent handleTaiwanResource(UriParser parser) {
        String path = parser.getPath(); // e.g., sights/台北市

        if (path.startsWith("sights/")) {
            String region = path.substring("sights/".length());
            if ("all".equals(region)) {
                return ResourceContent.json(sightService.findAll());
            }
            return ResourceContent.json(sightService.findByRegion(region));
        }

        if (path.startsWith("foods/")) {
            String region = path.substring("foods/".length());
            return ResourceContent.json(foodService.findByRegion(region));
        }

        // ... 其他 resources

        throw new ResourceNotFoundException(parser.getUri());
    }
}
```

### 4. Prompts 定義

Prompts 是預設的對話模板，幫助使用者快速開始。

```java
@Component
public class TaiwanPromptRegistry implements PromptRegistry {

    @Override
    public List<Prompt> listPrompts() {
        return List.of(
            Prompt.builder()
                .name("family-trip-planner")
                .description("家庭旅遊規劃助手")
                .build(),
            Prompt.builder()
                .name("food-tour-guide")
                .description("美食之旅導覽")
                .build(),
            Prompt.builder()
                .name("cultural-experience")
                .description("深度文化體驗規劃")
                .build()
        );
    }

    @Override
    public PromptTemplate getPrompt(String name, Map<String, String> args) {
        switch (name) {
            case "family-trip-planner":
                return buildFamilyTripPrompt(args);
            case "food-tour-guide":
                return buildFoodTourPrompt(args);
            case "cultural-experience":
                return buildCulturalPrompt(args);
            default:
                throw new PromptNotFoundException(name);
        }
    }

    private PromptTemplate buildFamilyTripPrompt(Map<String, String> args) {
        String region = args.get("region");
        String days = args.get("days");

        return PromptTemplate.builder()
            .messages(List.of(
                Message.system(
                    "你是一位專業的台灣家庭旅遊規劃師。" +
                    "請為家庭旅客規劃適合親子的行程，" +
                    "考慮景點的安全性、教育意義，以及孩童友善的美食選擇。"
                ),
                Message.user(
                    String.format(
                        "請幫我規劃 %s 的 %s 天家庭旅遊行程，" +
                        "家中有學齡兒童，希望行程兼顧教育與娛樂。",
                        region, days
                    )
                )
            ))
            .build();
    }

    private PromptTemplate buildFoodTourPrompt(Map<String, String> args) {
        String region = args.get("region");

        return PromptTemplate.builder()
            .messages(List.of(
                Message.system(
                    "你是一位在地美食專家，熟悉台灣各地的特色料理和小吃。" +
                    "請推薦道地的美食，包含歷史故事和品嚐建議。"
                ),
                Message.user(
                    String.format(
                        "我想深度品嚐 %s 的在地美食，" +
                        "請推薦必吃的餐廳和小吃，並規劃美食路線。",
                        region
                    )
                )
            ))
            .build();
    }
}
```

## 行程規劃演算法

### ItineraryService 核心邏輯

```java
@Service
public class ItineraryService {

    public ItineraryPlan planItinerary(PlanRequest request) {
        // 1. 收集候選項目
        List<Sight> candidateSights = collectCandidateSights(request);
        List<Food> candidateFoods = collectCandidateFoods(request);
        List<Festival> candidateFestivals = collectCandidateFestivals(request);

        // 2. 評分與排序
        List<ScoredItem> scoredItems = scoreAndRankItems(
            candidateSights, candidateFoods, candidateFestivals, request
        );

        // 3. 行程分配（依天數）
        List<DayPlan> dayPlans = allocateItemsToDays(scoredItems, request.getDays());

        // 4. 路線優化（減少移動時間）
        dayPlans = optimizeRoutes(dayPlans);

        // 5. 時間安排（考慮營業時間、停留時間）
        dayPlans = scheduleTimings(dayPlans);

        // 6. 預算分配
        BudgetBreakdown budget = calculateBudget(dayPlans, request.getBudget());

        // 7. 生成建議與提醒
        List<String> tips = generateTips(dayPlans, request);

        return ItineraryPlan.builder()
            .days(dayPlans)
            .budget(budget)
            .tips(tips)
            .build();
    }

    private double scoreItem(Item item, PlanRequest request) {
        double score = 0.0;

        // 評分因素：
        // 1. 匹配旅遊風格（30%）
        score += matchTravelStyle(item, request.getTravelStyle()) * 0.3;

        // 2. 受歡迎程度（20%）
        score += normalizeViewCount(item.getViewCount()) * 0.2;

        // 3. 評分（20%）
        score += item.getAverageRating() / 5.0 * 0.2;

        // 4. 符合偏好（20%）
        score += matchPreferences(item, request.getPreferences()) * 0.2;

        // 5. 獨特性（10%）
        score += item.getUniquenessScore() * 0.1;

        return score;
    }

    private List<DayPlan> optimizeRoutes(List<DayPlan> dayPlans) {
        // 使用貪婪演算法或模擬退火演算法
        // 最小化每天的總移動距離
        for (DayPlan day : dayPlans) {
            List<Item> items = day.getItems();
            List<Item> optimized = tspSolver.solve(items);
            day.setItems(optimized);
        }
        return dayPlans;
    }
}
```

### 評分系統

```java
public class ItemScorer {

    public double calculateScore(Item item, PlanRequest request) {
        Map<String, Double> weights = new HashMap<>();
        weights.put("travelStyle", 0.3);
        weights.put("popularity", 0.2);
        weights.put("rating", 0.2);
        weights.put("preferences", 0.2);
        weights.put("uniqueness", 0.1);

        double totalScore = 0.0;

        // 風格匹配
        totalScore += matchTravelStyle(item, request) * weights.get("travelStyle");

        // 受歡迎程度（標準化 view_count）
        totalScore += normalizePopularity(item) * weights.get("popularity");

        // 評分
        totalScore += item.getRating() / 5.0 * weights.get("rating");

        // 偏好匹配
        totalScore += matchPreferences(item, request) * weights.get("preferences");

        // 獨特性
        totalScore += calculateUniqueness(item) * weights.get("uniqueness");

        return totalScore;
    }

    private double matchTravelStyle(Item item, String travelStyle) {
        Map<String, List<String>> styleMapping = Map.of(
            "美食", List.of("小吃", "餐廳", "夜市", "傳統市場"),
            "文化", List.of("古蹟", "博物館", "廟宇", "傳統節慶"),
            "自然", List.of("國家公園", "海灘", "森林", "溫泉"),
            "親子", List.of("遊樂園", "動物園", "親子餐廳", "科學館")
        );

        List<String> relevantTags = styleMapping.getOrDefault(travelStyle, List.of());
        long matchCount = item.getTags().stream()
            .filter(relevantTags::contains)
            .count();

        return Math.min(1.0, matchCount / 3.0);
    }
}
```

## MCP Server 端點設計

### SSE Endpoint

```java
@RestController
@RequestMapping("/mcp")
public class McpController {

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        mcpServer.registerClient(emitter);

        emitter.onCompletion(() -> mcpServer.unregisterClient(emitter));
        emitter.onTimeout(() -> mcpServer.unregisterClient(emitter));

        return emitter;
    }

    @PostMapping("/message")
    public ResponseEntity<McpResponse> handleMessage(@RequestBody McpRequest request) {
        McpResponse response = mcpServer.handleRequest(request);
        return ResponseEntity.ok(response);
    }
}
```

## 使用範例

### 範例 1：簡單的景點搜尋

**使用者輸入**：
```
幫我找台北市的博物館
```

**AI 呼叫 Tool**：
```json
{
  "method": "tools/call",
  "params": {
    "name": "search_sights",
    "arguments": {
      "region": "台北市",
      "category": "博物館"
    }
  }
}
```

**MCP Server 回應**：
```json
{
  "content": [
    {
      "type": "text",
      "text": "找到 15 個台北市的博物館"
    },
    {
      "type": "resource",
      "resource": {
        "uri": "taiwan://sights/台北市?category=博物館",
        "mimeType": "application/json",
        "text": "[{\"sightName\":\"國立故宮博物院\",...}]"
      }
    }
  ]
}
```

### 範例 2：複雜的行程規劃

**使用者輸入**：
```
幫我規劃台南三天兩夜的美食之旅，預算 15000 元，
我喜歡傳統小吃和在地文化，不要太多爬山行程
```

**AI 的處理流程**：

1. **呼叫 Tool: search_foods**
```json
{
  "name": "search_foods",
  "arguments": {
    "region": "台南市",
    "category": "小吃"
  }
}
```

2. **呼叫 Tool: search_sights**
```json
{
  "name": "search_sights",
  "arguments": {
    "region": "台南市",
    "tags": ["歷史", "文化"],
    "category": "古蹟"
  }
}
```

3. **呼叫 Tool: plan_itinerary**
```json
{
  "name": "plan_itinerary",
  "arguments": {
    "regions": ["台南市"],
    "days": 3,
    "travel_style": "美食",
    "budget": 15000,
    "preferences": ["傳統小吃", "在地文化", "避免爬山"]
  }
}
```

**MCP Server 回應**：
```json
{
  "itinerary": {
    "days": [
      {
        "day": 1,
        "theme": "台南古城巡禮",
        "items": [
          {
            "time": "09:00",
            "type": "sight",
            "name": "赤崁樓",
            "duration": 60,
            "description": "台南代表性古蹟"
          },
          {
            "time": "11:00",
            "type": "food",
            "name": "度小月擔仔麵",
            "duration": 45,
            "description": "百年老店，必吃擔仔麵"
          },
          // ... 更多項目
        ]
      },
      // ... Day 2, Day 3
    ],
    "budget_breakdown": {
      "accommodation": 4000,
      "food": 6000,
      "transportation": 2000,
      "tickets": 1000,
      "reserve": 2000
    },
    "tips": [
      "台南夏天很熱，記得防曬和補充水分",
      "許多老店只營業到下午，建議早點出發",
      "可以租電動機車方便移動"
    ]
  }
}
```

## 部署配置

### application.yml

```yaml
mcp:
  server:
    enabled: true
    transport: sse
    endpoint: /mcp/sse
    name: taiwan-travel-mcp
    version: 1.0.0
  tools:
    enabled: true
    timeout: 30000  # 30 秒
  resources:
    cache-ttl: 3600  # 1 小時
  prompts:
    enabled: true
```

### MCP 客戶端配置（Claude Desktop）

```json
{
  "mcpServers": {
    "taiwan-travel": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/myproject.jar",
        "--mcp.mode=stdio"
      ],
      "env": {
        "SPRING_PROFILES_ACTIVE": "mcp"
      }
    }
  }
}
```

## 測試策略

### 單元測試

```java
@Test
public void testSearchSightsTool() {
    SearchSightsTool tool = new SearchSightsTool();
    tool.setRegion("台北市");
    tool.setCategory("博物館");

    ToolResult result = tool.execute();

    assertTrue(result.isSuccess());
    assertFalse(result.getData().isEmpty());
}
```

### 整合測試

```java
@SpringBootTest
public class McpServerIntegrationTest {

    @Test
    public void testFullItineraryPlanning() {
        McpRequest request = McpRequest.builder()
            .method("tools/call")
            .params(Map.of(
                "name", "plan_itinerary",
                "arguments", Map.of(
                    "regions", List.of("台南市"),
                    "days", 3,
                    "travel_style", "美食"
                )
            ))
            .build();

        McpResponse response = mcpServer.handleRequest(request);

        assertNotNull(response);
        assertTrue(response.hasContent());
    }
}
```

## 效能考量

1. **快取策略** - Resources 快取 1 小時
2. **非同步處理** - 行程規劃使用非同步
3. **資料庫查詢優化** - 使用索引和 pagination
4. **連線池管理** - 限制同時連線數
5. **請求逾時** - 30 秒逾時保護

## 安全性

1. **輸入驗證** - 驗證所有 Tool 參數
2. **速率限制** - 防止濫用
3. **資料清理** - 避免 SQL injection
4. **CORS 配置** - 限制來源

## 監控與日誌

```java
@Aspect
@Component
public class McpLoggingAspect {

    @Around("@annotation(McpTool)")
    public Object logToolExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String toolName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("Tool executed: {}, duration: {}ms", toolName, duration);

            return result;
        } catch (Exception e) {
            log.error("Tool failed: {}, error: {}", toolName, e.getMessage());
            throw e;
        }
    }
}
```

## 未來擴展

1. **多語言支援** - 支援英文、日文等
2. **天氣整合** - 根據天氣調整行程
3. **即時交通** - 整合 Google Maps 即時路況
4. **社群功能** - 分享和評論行程
5. **個人化推薦** - 基於歷史偏好學習
6. **語音互動** - 語音輸入和導覽

## 總結

這個 MCP Server 設計提供了：

✅ **完整的 MCP 協議實作**
✅ **智慧行程規劃演算法**
✅ **靈活的 Tool/Resource/Prompt 系統**
✅ **與現有 Spring Boot 架構完美整合**
✅ **易於擴展和維護**

讓使用者可以用自然語言輕鬆規劃台灣旅遊，體驗 AI 驅動的智慧旅遊助手！
