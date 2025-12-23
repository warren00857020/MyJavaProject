# 本地測試指南

## 📋 前置準備

### 1. 確認 PostgreSQL 已安裝並啟動

```bash
# 檢查 PostgreSQL 是否執行中
psql --version

# Windows 可以檢查服務
Get-Service postgresql*
```

### 2. 建立資料庫

```bash
# 連接到 PostgreSQL
psql -U postgres

# 建立資料庫
CREATE DATABASE mydb;

# 退出
\q
```

### 3. 確認 application.properties 設定

檢查 `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=postgres  # 改成你的密碼

spring.jpa.hibernate.ddl-auto=update  # 自動建立/更新資料表
spring.jpa.show-sql=true              # 顯示 SQL 語句（方便除錯）
```

## 🚀 啟動應用程式

### 方法 1: 使用 Maven（命令列）

```bash
# Windows
.\mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 方法 2: 使用 IDE（IntelliJ IDEA）

1. 開啟專案
2. 找到 `MyJavaProjectApplication.java`
3. 點擊綠色執行按鈕
4. 或按 `Shift + F10`

### 方法 3: 使用 JAR 檔案

```bash
# 先編譯
.\mvnw clean package

# 執行
java -jar target/myJavaProject-0.0.1-SNAPSHOT.jar
```

## ✅ 驗證啟動成功

啟動成功後，應該會看到類似的訊息：
```
Started MyJavaProjectApplication in X.XXX seconds
```

檢查資料表是否建立：
```bash
psql -U postgres -d mydb

# 查看所有資料表
\dt

# 應該會看到：
# - regions
# - sights
# - foods
# - festivals
```

## 🧪 初始化測試資料

### 1. 初始化縣市資料

```bash
# 連接到資料庫
psql -U postgres -d mydb

# 執行初始化腳本
\i src/main/resources/data-init.sql

# 或直接複製貼上 data-init.sql 的內容
```

### 2. 驗證資料已建立

```sql
-- 查詢所有縣市
SELECT * FROM regions;

-- 應該會看到 22 筆縣市資料
```

## 🔧 測試 API

### 使用 curl 測試

#### 1. 新增景點

```bash
curl -X POST http://localhost:8080/sights \
  -H "Content-Type: application/json" \
  -d '{
    "sightName": "中正紀念堂",
    "zone": "中正區",
    "category": "古蹟",
    "photoURL": "https://example.com/photo.jpg",
    "description": "台北著名地標",
    "address": "台北市中正區中山南路21號"
  }'
```

#### 2. 查詢所有景點

```bash
curl http://localhost:8080/sights
```

#### 3. 根據行政區查詢

```bash
curl "http://localhost:8080/sights?keyword=中正區"
```

### 使用 Postman 測試

1. 下載並安裝 [Postman](https://www.postman.com/)
2. 建立新的 Request
3. 選擇 HTTP 方法 (GET/POST/PUT/DELETE)
4. 輸入 URL: `http://localhost:8080/sights`
5. 設定 Body (JSON 格式)
6. 發送請求

### 使用瀏覽器測試 (GET 請求)

直接在瀏覽器輸入：
```
http://localhost:8080/sights
```

## 📊 直接操作資料庫測試

### 1. 手動新增測試資料

```sql
-- 連接到資料庫
psql -U postgres -d mydb

-- 新增景點
INSERT INTO sights (
    sight_name, region_id, zone, category,
    photo_url, description, address,
    created_at, updated_at
) VALUES (
    '國立故宮博物院', 1, '士林區', '博物館',
    'https://example.com/palace.jpg',
    '世界著名博物館，收藏大量中華文物',
    '台北市士林區至善路二段221號',
    NOW(), NOW()
);

-- 新增美食
INSERT INTO foods (
    food_name, region_id, zone, category, cuisine_type,
    description, address, price_range,
    created_at, updated_at
) VALUES (
    '鼎泰豐', 1, '大安區', '餐廳', '台菜',
    '世界知名小籠包餐廳',
    '台北市大安區信義路二段194號',
    '$$',
    NOW(), NOW()
);

-- 新增節慶
INSERT INTO festivals (
    festival_name, region_id, category,
    description, start_date, end_date, month_held,
    created_at, updated_at
) VALUES (
    '台北燈節', 1, '傳統節慶',
    '每年元宵節舉辦的大型燈會活動',
    '2025-02-12', '2025-02-24', 2,
    NOW(), NOW()
);
```

### 2. 查詢測試

```sql
-- 查詢所有景點
SELECT * FROM sights;

-- 根據縣市查詢景點（台北市 region_id = 1）
SELECT * FROM sights WHERE region_id = 1;

-- 查詢所有美食
SELECT * FROM foods;

-- 查詢特定價格範圍的美食
SELECT * FROM foods WHERE price_range = '$$';

-- 查詢本月的節慶
SELECT * FROM festivals WHERE month_held = EXTRACT(MONTH FROM CURRENT_DATE);

-- 聯合查詢：縣市 + 景點
SELECT r.name as region_name, s.sight_name, s.category
FROM regions r
JOIN sights s ON r.id = s.region_id;
```

## 🐛 常見問題排除

### 問題 1: 連接資料庫失敗

```
Error: Connection refused
```

**解決方法**:
1. 確認 PostgreSQL 服務已啟動
2. 檢查 `application.properties` 中的連線設定
3. 確認資料庫 `mydb` 已建立

### 問題 2: 陣列欄位不支援

```
Error: Cannot map TEXT[] type
```

**解決方法**:
這是正常的，部分 JPA 實作可能不支援 PostgreSQL 陣列。可以：
1. 使用 `@Type` annotation (需要額外依賴)
2. 或先將陣列欄位改為 `TEXT`，用逗號分隔字串

### 問題 3: JSONB 欄位報錯

```
Error: Unknown column type JSONB
```

**解決方法**:
將 `columnDefinition = "JSONB"` 改為 `columnDefinition = "TEXT"`，暫時儲存 JSON 字串。

### 問題 4: Maven 命令找不到

```
mvn: command not found
```

**解決方法**:
使用專案內建的 Maven Wrapper:
```bash
# Windows
.\mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

## 📝 下一步

測試成功後，你可以：

1. ✅ **Phase 1 完成** - 資料基礎建設
2. 🚀 **開始 Phase 2** - 實作爬蟲系統
3. 🎨 **建立 Controller** - 擴展 REST API
4. 🧪 **撰寫測試** - 單元測試與整合測試

## 💡 小技巧

### 啟用 SQL 日誌

在 `application.properties` 中設定：
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### 使用 H2 記憶體資料庫（快速測試）

如果不想設定 PostgreSQL，可以暫時使用 H2：

1. 在 `pom.xml` 加入：
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. 修改 `application.properties`：
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.h2.console.enabled=true
```

3. 訪問 H2 Console: http://localhost:8080/h2-console

---

祝測試順利！有任何問題隨時詢問。
