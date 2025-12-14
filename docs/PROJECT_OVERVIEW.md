# 台灣智慧旅遊 AI 助手 - 專案總覽

## 🎯 專案願景

打造一個智慧旅遊平台，結合：
- 📊 **完整的台灣旅遊資料庫**（景點、美食、節慶）
- 🤖 **AI 驅動的行程規劃**（透過 MCP Server）
- 🗣️ **自然語言互動**（讓使用者用聊天方式規劃旅遊）

## 🏗️ 系統架構

```
使用者
  ↓ (自然語言)
Claude AI / AI Assistant
  ↓ (MCP Protocol)
Taiwan Travel MCP Server
  ↓ (REST API)
Spring Boot Backend
  ↓ (SQL)
PostgreSQL Database
  ↑ (爬蟲)
Taiwan.net.tw
```

## 📚 設計文件

已完成的設計文件：

### 1. [資料庫 Schema 設計](./database-schema.md)
- ✅ 7 個主要資料表設計
- ✅ 支援景點、美食、節慶、行程
- ✅ 完整的索引和關聯設計
- ✅ JSONB 彈性欄位設計

**核心資料表**：
- `regions` - 縣市/區域
- `sights` - 景點（擴展版）
- `foods` - 美食
- `festivals` - 傳統節慶/活動
- `itineraries` - 使用者行程
- `itinerary_items` - 行程項目
- `reviews` - 評論（選配）

### 2. [爬蟲系統設計](./crawler-design.md)
- ✅ 通用爬蟲基類架構
- ✅ 三種專門爬蟲（景點、美食、節慶）
- ✅ 反爬蟲策略（延遲、User-Agent 輪換、重試）
- ✅ 資料清洗與驗證
- ✅ 排程與增量更新

**爬蟲特性**：
- 分階段爬取（列表頁 → 詳細頁）
- 批次處理與並行爬取
- 完整的錯誤處理與日誌
- 預估全台爬取時間：6-12 小時

### 3. [MCP Server 設計](./mcp-server-design.md)
- ✅ 完整的 MCP 協議實作
- ✅ 6 個核心 Tools 設計
- ✅ Resource 與 Prompt 系統
- ✅ 智慧行程規劃演算法
- ✅ 評分與推薦系統

**核心功能**：
- `search_sights` - 搜尋景點
- `search_foods` - 搜尋美食
- `search_festivals` - 搜尋節慶
- `plan_itinerary` - 智慧行程規劃 ⭐
- `calculate_route` - 路線計算
- `get_recommendations` - 個人化推薦

## 🚀 實作優先順序建議

### Phase 1: 資料基礎建設（1-2 週）
1. ✅ 資料庫 Schema 實作
   - 建立 migration scripts
   - 擴展現有 Sight entity
   - 新增 Food, Festival entities
2. ✅ 基礎 Repository 層
3. ✅ 基礎 Service 層

### Phase 2: 爬蟲系統（1-2 週）
1. ✅ 實作通用爬蟲基類
2. ✅ 實作景點爬蟲（擴展現有的）
3. ✅ 實作美食爬蟲
4. ✅ 實作節慶爬蟲
5. ✅ 爬蟲 Controller 和排程
6. ✅ 執行首次完整爬取

### Phase 3: REST API（1 週）
1. ✅ 擴展現有的 Controller
2. ✅ 新增 FoodController
3. ✅ 新增 FestivalController
4. ✅ 新增 ItineraryController
5. ✅ API 文件（Swagger）

### Phase 4: MCP Server（2-3 週）⭐ 核心亮點
1. ✅ MCP 協議基礎實作
2. ✅ Tool Registry 系統
3. ✅ 實作基礎 Tools（search_sights, search_foods）
4. ✅ 實作進階 Tools（plan_itinerary）
5. ✅ Resource Provider
6. ✅ Prompt Templates
7. ✅ SSE 端點

### Phase 5: 行程規劃演算法（1-2 週）
1. ✅ 評分系統
2. ✅ 路線優化（TSP solver）
3. ✅ 時間排程
4. ✅ 預算分配
5. ✅ 推薦演算法

### Phase 6: 測試與優化（1 週）
1. ✅ 單元測試
2. ✅ 整合測試
3. ✅ 效能優化
4. ✅ 文件完善

## 💡 技術亮點

### 1. 智慧行程規劃演算法
- 多因素評分系統（風格、人氣、評分、偏好）
- 路線優化（最小化移動距離）
- 時間智慧排程（考慮營業時間）
- 預算智慧分配

### 2. MCP 協議整合
- 讓 AI 能直接呼叫你的系統
- 自然語言互動介面
- 標準化的工具與資源定義

### 3. 大規模資料爬取
- 全台灣 22 縣市資料
- 預估 2000+ 景點
- 1000+ 美食據點
- 100+ 節慶活動

### 4. 彈性的資料模型
- JSONB 欄位儲存非結構化資料
- 陣列欄位支援多值（標籤、照片）
- 地理位置資料（經緯度）

## 📊 預期成果

### 使用案例

**範例 1：簡單查詢**
```
使用者：「幫我找台南的牛肉湯」
AI：調用 search_foods(region="台南市", keyword="牛肉湯")
系統：返回 15 家台南牛肉湯餐廳
```

**範例 2：複雜行程規劃**
```
使用者：「幫我規劃台南三天兩夜美食之旅，預算 15000」
AI：
  1. search_foods(region="台南市")
  2. search_sights(region="台南市", tags=["歷史","文化"])
  3. plan_itinerary(regions=["台南市"], days=3, style="美食", budget=15000)
系統：生成完整的三天行程，包含：
  - 每天 4-5 個景點/美食
  - 優化的移動路線
  - 詳細的時間安排
  - 預算分配建議
  - 旅遊小提醒
```

### 資料規模預估

| 類型 | 預估數量 | 資料來源 |
|------|----------|----------|
| 縣市 | 22 | 固定資料 |
| 景點 | 2,000+ | taiwan.net.tw |
| 美食 | 1,000+ | taiwan.net.tw |
| 節慶 | 100+ | taiwan.net.tw |
| 標籤 | 100+ | 自動提取 |

## 🛠️ 開發工具與技術棧

### 後端
- Java 17
- Spring Boot 3.1.4
- Spring Data JPA
- PostgreSQL 16
- JSoup (爬蟲)

### MCP
- JSON-RPC 2.0
- Server-Sent Events (SSE)
- Custom MCP SDK (Java)

### 部署
- Docker
- Google Cloud Run
- GitHub Actions CI/CD

### 測試
- JUnit 5
- Spring Boot Test
- Mockito

## 📈 效能目標

- API 回應時間：< 200ms（簡單查詢）
- 行程規劃時間：< 5s（3天行程）
- 爬蟲速率：2-3s/item（避免被封鎖）
- 資料庫查詢：使用索引優化
- 並行處理：支援多使用者同時使用

## 🔒 安全性考量

1. **輸入驗證** - 所有 API 參數驗證
2. **SQL Injection 防護** - 使用 JPA/Prepared Statements
3. **速率限制** - 防止 API 濫用
4. **資料清理** - 爬蟲資料驗證與清洗
5. **CORS 配置** - 限制允許的來源

## 🎓 學習價值

這個專案展示了：

1. ✅ **完整的 Spring Boot 應用開發**
2. ✅ **大規模資料爬取與處理**
3. ✅ **AI 整合（MCP 協議）**
4. ✅ **演算法設計（行程規劃、路線優化）**
5. ✅ **RESTful API 設計**
6. ✅ **資料庫設計與優化**
7. ✅ **Docker 容器化部署**
8. ✅ **CI/CD 自動化**

## 🚦 下一步建議

### 選項 A：從頭開始完整實作（建議初學者）
1. 先實作資料庫 schema
2. 建立基礎的 Entity 和 Repository
3. 實作簡單的爬蟲測試
4. 逐步加入 MCP Server

### 選項 B：快速原型（建議有經驗者）
1. 先用模擬資料測試 MCP Server
2. 實作核心的 plan_itinerary 演算法
3. 再補充真實的資料來源

### 選項 C：分模組開發（建議團隊協作）
1. 成員 A：負責爬蟲系統
2. 成員 B：負責 MCP Server
3. 成員 C：負責行程規劃演算法

## 📞 你想要怎麼開始？

我可以幫你：

**A. 開始實作資料庫**
- 建立 Entity classes
- 建立 Repository interfaces
- 建立 migration scripts

**B. 開始實作爬蟲**
- 實作通用爬蟲基類
- 測試 taiwan.net.tw 網站結構
- 實作第一個爬蟲

**C. 開始實作 MCP Server**
- 建立 MCP 協議框架
- 實作第一個 Tool
- 測試與 Claude 整合

**D. 先看完整的實作計畫**
- 我可以寫一份更詳細的逐步實作指南
- 包含每個檔案的範例程式碼

**E. 先做快速 Demo**
- 用少量模擬資料
- 快速展示 MCP Server 功能
- 驗證概念可行性

你想從哪裡開始？
