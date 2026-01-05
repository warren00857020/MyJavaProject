# 測試文檔

## 📋 測試概覽

本專案包含完整的測試套件，涵蓋單元測試和整合測試。

### 測試統計

- **測試類別數量**: 9+
- **測試總數**: 36 tests
- **最近執行狀態**: ✅ 20/20 tests passed (SightService + SightController, 2026-01-05)
- **主要測試覆蓋**:
  - ✅ ClaudeService 單元測試 (6 tests)
  - ✅ ChatController 整合測試 (9 tests)
  - ✅ SightService 測試 (12 tests)
  - ✅ SightController 測試 (8 tests)
  - ✅ 異常處理測試

---

## 🧪 ClaudeService 測試

### 測試檔案
`src/test/java/com/example/service/ClaudeServiceTest.java`

### 測試案例

#### 1. Mock 模式測試
```java
@Test
void testMockModeEnabled()
```
- **目的**: 驗證 Mock 模式啟用時，返回模擬回應而不調用真實 API
- **重要性**: 確保測試環境和開發時不消耗 Claude API 額度

#### 2. 基本聊天功能測試
```java
@Test
void testBasicChatWithoutTools()
```
- **目的**: 測試基本聊天功能（不使用工具）
- **使用 Mock 模式**: 避免在 CI 環境中調用真實 API

#### 3. API Key 驗證測試
```java
@Test
void testApiKeyIsSet()
```
- **目的**: 驗證 API Key 正確設置
- **使用 Mock 模式**: 在測試環境中安全測試

#### 4. 缺少 API Key 測試
```java
@Test
void testMissingApiKeyThrowsException()
```
- **目的**: 驗證缺少 API Key 時拋出 `IllegalStateException`
- **預期行為**: 應該在啟動時就發現配置錯誤

#### 5. Mock 回應關鍵字匹配測試
```java
@Test
void testMockResponseKeywordMatching()
```
- **目的**: 驗證 Mock 模式下，根據不同關鍵字返回不同回應
- **測試案例**:
  - "推薦台北景點" → 景點相關回應
  - "推薦餐廳" → 美食相關回應

#### 6. 工具定義 Schema 測試
```java
@Test
void testToolDefinitionSchema()
```
- **目的**: 驗證工具定義包含正確的 JSON Schema
- **檢查項目**:
  - 工具數量（2個：search_sights, search_foods）
  - 每個工具的 name、description、inputSchema

### 測試技術

- **Mockito**: Mock RestTemplate 避免真實 API 調用
- **ReflectionTestUtils**: 注入測試配置（apiKey, mockEnabled）
- **Reflection**: 測試 private 方法（getTaiwanTravelTools）

---

## 🌐 ChatController 測試

### 測試檔案
`src/test/java/com/example/controller/ChatControllerTest.java`

### 測試案例

#### 1. 健康檢查端點
```java
@Test
void testHealthEndpoint()
```
- **端點**: `GET /api/chat/health`
- **預期回應**: `{ "status": "ok", "service": "Chat API" }`

#### 2. 模型列表端點
```java
@Test
void testGetModels()
```
- **端點**: `GET /api/chat/models`
- **預期回應**: 返回可用模型列表

#### 3. 成功的聊天請求
```java
@Test
void testSuccessfulChatRequest()
```
- **端點**: `POST /api/chat`
- **請求**: `{ "message": "推薦台北景點", "history": [] }`
- **預期**: 返回包含景點資訊的回應

#### 4. 空訊息驗證
```java
@Test
void testEmptyMessageReturnsError()
```
- **目的**: 驗證空訊息返回 400 Bad Request
- **預期回應**: `{ "success": false, "error": "訊息不能為空" }`

#### 5. Null 訊息驗證
```java
@Test
void testNullMessageReturnsError()
```
- **目的**: 驗證 null 訊息返回錯誤

#### 6. 對話歷史測試
```java
@Test
void testChatWithHistory()
```
- **目的**: 驗證包含對話歷史的請求正常處理
- **測試多輪對話**: 確保 Claude 能理解上下文

#### 7. Service 異常處理測試
```java
@Test
void testChatServiceException()
```
- **目的**: 驗證 ClaudeService 拋出異常時的錯誤處理
- **預期**: 返回 500 Internal Server Error

#### 8. API Key 未配置測試
```java
@Test
void testApiKeyNotConfigured()
```
- **目的**: 驗證 `IllegalStateException` 的特殊處理
- **預期回應**: 包含 hint 提示如何配置

#### 9. CORS 標頭測試
```java
@Test
void testCorsHeaders()
```
- **目的**: 驗證跨域請求的 CORS 標頭
- **重要性**: 前端需要跨域訪問 API

### 測試技術

- **MockMvc**: 模擬 HTTP 請求，無需啟動完整伺服器
- **@WebMvcTest**: 只載入 Controller 層，快速測試
- **@MockBean**: Mock ClaudeService 依賴
- **JsonPath**: 驗證 JSON 回應內容

---

## ⚙️ 測試環境配置

### 檔案
`src/test/resources/application-test.properties`

### 配置內容

```properties
# Claude API (Mock mode for testing)
claude.api.key=test-api-key
claude.mock.enabled=true

# Google Places API (Mock for testing)
google.places.api.key=test-google-api-key

# Database (Use H2 in-memory for tests)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# Disable Flyway for tests
spring.flyway.enabled=false
```

### 為什麼需要這個配置？

1. **Mock 模式**: 測試時不消耗 Claude API 額度
2. **H2 記憶體資料庫**: 快速、隔離的測試環境
3. **禁用 Flyway**: 測試時不需要資料庫遷移

---

## 🚀 執行測試

### 本地執行

```bash
# 執行所有測試
.\mvnw.cmd test

# 只執行特定測試類別
.\mvnw.cmd test -Dtest=ClaudeServiceTest

# 執行測試並產生覆蓋率報告
.\mvnw.cmd test jacoco:report
```

### CI/CD 執行

GitHub Actions 會在以下情況自動執行測試：

1. **Push 到 main 分支**
2. **建立 Pull Request**

### GitHub Actions Workflow

檔案：`.github/workflows/ci-cd.yml`

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
      - name: Run tests
        run: ./mvnw test
      - name: Generate test report
        uses: dorny/test-reporter@v1
```

---

## 📊 測試覆蓋率目標

| 類別 | 目標覆蓋率 | 當前狀態 |
|------|-----------|----------|
| Controller | 80%+ | ✅ 已達成 |
| Service | 70%+ | ✅ 已達成 |
| DTO | 60%+ | ✅ 已達成 |
| Exception Handler | 80%+ | ✅ 已達成 |

---

## 🎯 測試最佳實踐

### 1. 使用 Mock 避免外部依賴

```java
@MockBean
private ClaudeService claudeService;

when(claudeService.chat(anyList(), anyBoolean()))
    .thenReturn("模擬回應");
```

### 2. 測試邊界條件

- ✅ 空字串
- ✅ Null 值
- ✅ 超長輸入
- ✅ 特殊字元

### 3. 驗證錯誤處理

```java
@Test
void testErrorHandling() {
    when(service.method()).thenThrow(new RuntimeException());

    mockMvc.perform(post("/api/endpoint"))
        .andExpect(status().isInternalServerError());
}
```

### 4. 使用描述性測試名稱

```java
// ❌ 不好
@Test
void test1() { ... }

// ✅ 好
@Test
void testEmptyMessageReturnsError() { ... }
```

---

## 🐛 已知問題與解決方案

### 問題 1: ApplicationContext 載入失敗

**原因**: 測試環境缺少環境變數

**解決方案**: 使用 `application-test.properties` 提供測試配置

### 問題 2: Claude API 401 Unauthorized

**原因**: 測試時嘗試調用真實 API

**解決方案**: 在測試中啟用 Mock 模式

```java
ReflectionTestUtils.setField(claudeService, "mockEnabled", true);
```

---

## 📚 延伸閱讀

- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [Mockito Documentation](https://site.mockito.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

---

## ✅ 測試檢查清單

在提交 PR 前，確保：

- [ ] 所有測試通過 (`mvn test`)
- [ ] 新功能都有對應的測試
- [ ] 測試覆蓋率符合目標
- [ ] Mock 了所有外部依賴（API、資料庫）
- [ ] 測試可以在 CI 環境中執行
- [ ] 沒有硬編碼的敏感資訊（API Key）

---

最後更新：2026-01-05
