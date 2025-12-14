# 台灣旅遊資料庫 Schema 設計

## 資料表結構

### 1. regions (縣市/區域)
```sql
CREATE TABLE regions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,          -- 縣市名稱，如：台北市、台南市
    code VARCHAR(10) NOT NULL UNIQUE,          -- 縣市代碼，如：TPE, TNN
    region_type VARCHAR(20) NOT NULL,          -- 類型：city, county
    description TEXT,                           -- 區域描述
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. sights (景點) - 擴展版
```sql
CREATE TABLE sights (
    id BIGSERIAL PRIMARY KEY,
    sight_name VARCHAR(255) NOT NULL,           -- 景點名稱
    region_id BIGINT REFERENCES regions(id),    -- 關聯縣市
    zone VARCHAR(100),                          -- 行政區，如：中正區
    category VARCHAR(100),                      -- 分類：古蹟、自然景觀、博物館等
    photo_url VARCHAR(1024),                    -- 主要照片 URL
    photo_urls TEXT[],                          -- 多張照片 URLs（陣列）
    description TEXT,                           -- 景點描述
    address VARCHAR(255),                       -- 地址
    latitude DECIMAL(10, 8),                    -- 緯度
    longitude DECIMAL(11, 8),                   -- 經度
    opening_hours JSONB,                        -- 營業時間（JSON 格式）
    ticket_price VARCHAR(100),                  -- 票價資訊
    official_website VARCHAR(512),              -- 官方網站
    phone VARCHAR(50),                          -- 聯絡電話
    tags TEXT[],                                -- 標籤陣列：親子、網美、歷史等
    recommended_duration INTEGER,               -- 建議停留時間（分鐘）
    source_url VARCHAR(512),                    -- 資料來源 URL
    is_verified BOOLEAN DEFAULT false,          -- 是否已驗證
    view_count INTEGER DEFAULT 0,               -- 瀏覽次數
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(sight_name, region_id)
);

CREATE INDEX idx_sights_region ON sights(region_id);
CREATE INDEX idx_sights_category ON sights(category);
CREATE INDEX idx_sights_tags ON sights USING GIN(tags);
```

### 3. foods (美食)
```sql
CREATE TABLE foods (
    id BIGSERIAL PRIMARY KEY,
    food_name VARCHAR(255) NOT NULL,            -- 美食/餐廳名稱
    region_id BIGINT REFERENCES regions(id),    -- 關聯縣市
    zone VARCHAR(100),                          -- 行政區
    category VARCHAR(100),                      -- 分類：小吃、餐廳、甜點、夜市等
    cuisine_type VARCHAR(100),                  -- 菜系：台菜、客家菜、原住民料理等
    photo_url VARCHAR(1024),                    -- 主要照片 URL
    photo_urls TEXT[],                          -- 多張照片 URLs
    description TEXT,                           -- 美食描述
    address VARCHAR(255),                       -- 地址
    latitude DECIMAL(10, 8),                    -- 緯度
    longitude DECIMAL(11, 8),                   -- 經度
    opening_hours JSONB,                        -- 營業時間
    price_range VARCHAR(50),                    -- 價格範圍：$, $$, $$$
    signature_dishes TEXT[],                    -- 招牌菜色陣列
    phone VARCHAR(50),                          -- 聯絡電話
    official_website VARCHAR(512),              -- 官方網站
    tags TEXT[],                                -- 標籤：米其林、必吃、在地推薦等
    avg_meal_duration INTEGER,                  -- 平均用餐時間（分鐘）
    source_url VARCHAR(512),                    -- 資料來源 URL
    is_verified BOOLEAN DEFAULT false,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(food_name, address)
);

CREATE INDEX idx_foods_region ON foods(region_id);
CREATE INDEX idx_foods_category ON foods(category);
CREATE INDEX idx_foods_cuisine_type ON foods(cuisine_type);
CREATE INDEX idx_foods_tags ON foods USING GIN(tags);
```

### 4. festivals (傳統節慶/活動)
```sql
CREATE TABLE festivals (
    id BIGSERIAL PRIMARY KEY,
    festival_name VARCHAR(255) NOT NULL,        -- 節慶名稱
    region_id BIGINT REFERENCES regions(id),    -- 關聯縣市
    zone VARCHAR(100),                          -- 舉辦區域
    category VARCHAR(100),                      -- 分類：傳統節慶、音樂祭、藝術展等
    photo_url VARCHAR(1024),                    -- 主要照片 URL
    photo_urls TEXT[],                          -- 多張照片 URLs
    description TEXT,                           -- 節慶描述
    location VARCHAR(255),                      -- 舉辦地點
    address VARCHAR(255),                       -- 地址
    latitude DECIMAL(10, 8),                    -- 緯度
    longitude DECIMAL(11, 8),                   -- 經度
    start_date DATE,                            -- 開始日期
    end_date DATE,                              -- 結束日期
    recurring_pattern VARCHAR(100),             -- 週期性：annual, monthly, seasonal
    month_held INTEGER,                         -- 舉辦月份（農曆或國曆）
    is_lunar BOOLEAN DEFAULT false,             -- 是否為農曆
    event_schedule JSONB,                       -- 活動時程（JSON 格式）
    official_website VARCHAR(512),              -- 官方網站
    contact_info VARCHAR(255),                  -- 聯絡資訊
    tags TEXT[],                                -- 標籤：UNESCO、國定、地方特色等
    expected_duration INTEGER,                  -- 預計參與時間（分鐘）
    ticket_required BOOLEAN DEFAULT false,      -- 是否需要門票
    ticket_info TEXT,                           -- 票務資訊
    source_url VARCHAR(512),                    -- 資料來源 URL
    is_verified BOOLEAN DEFAULT false,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_festivals_region ON festivals(region_id);
CREATE INDEX idx_festivals_category ON festivals(category);
CREATE INDEX idx_festivals_date ON festivals(start_date, end_date);
CREATE INDEX idx_festivals_tags ON festivals USING GIN(tags);
```

### 5. itineraries (使用者行程)
```sql
CREATE TABLE itineraries (
    id BIGSERIAL PRIMARY KEY,
    itinerary_name VARCHAR(255) NOT NULL,       -- 行程名稱
    user_query TEXT,                            -- 使用者原始查詢
    region_ids BIGINT[],                        -- 涵蓋的縣市 IDs
    total_days INTEGER NOT NULL,                -- 總天數
    total_budget DECIMAL(10, 2),                -- 預算
    travel_style VARCHAR(100),                  -- 旅遊風格：美食、文化、自然等
    summary TEXT,                               -- 行程摘要
    ai_suggestions TEXT,                        -- AI 建議
    itinerary_data JSONB,                       -- 完整行程資料（JSON）
    is_public BOOLEAN DEFAULT false,            -- 是否公開
    share_code VARCHAR(50) UNIQUE,              -- 分享代碼
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_itineraries_region ON itineraries USING GIN(region_ids);
CREATE INDEX idx_itineraries_travel_style ON itineraries(travel_style);
```

### 6. itinerary_items (行程項目)
```sql
CREATE TABLE itinerary_items (
    id BIGSERIAL PRIMARY KEY,
    itinerary_id BIGINT REFERENCES itineraries(id) ON DELETE CASCADE,
    day_number INTEGER NOT NULL,                -- 第幾天
    item_order INTEGER NOT NULL,                -- 當天的順序
    item_type VARCHAR(50) NOT NULL,             -- sight, food, festival, transportation
    item_id BIGINT,                             -- 關聯的項目 ID（景點/美食/節慶）
    custom_title VARCHAR(255),                  -- 自訂標題（如交通）
    custom_description TEXT,                    -- 自訂描述
    start_time TIME,                            -- 開始時間
    end_time TIME,                              -- 結束時間
    duration INTEGER,                           -- 停留時間（分鐘）
    notes TEXT,                                 -- 備註
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_itinerary_items_itinerary ON itinerary_items(itinerary_id);
CREATE INDEX idx_itinerary_items_day ON itinerary_items(day_number);
```

### 7. reviews (評論) - 選配
```sql
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    item_type VARCHAR(50) NOT NULL,             -- sight, food, festival
    item_id BIGINT NOT NULL,                    -- 項目 ID
    user_name VARCHAR(100),                     -- 使用者名稱（匿名可用）
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,                               -- 評論內容
    visit_date DATE,                            -- 造訪日期
    is_verified BOOLEAN DEFAULT false,          -- 是否驗證過
    helpful_count INTEGER DEFAULT 0,            -- 有幫助數
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_item ON reviews(item_type, item_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
```

## 資料關聯圖

```
regions (縣市)
    ↓ (1:N)
    ├── sights (景點)
    ├── foods (美食)
    └── festivals (節慶)

itineraries (行程)
    ↓ (1:N)
itinerary_items (行程項目)
    ↓ (N:1)
    ├── sights
    ├── foods
    └── festivals
```

## JSON 欄位範例

### opening_hours (JSONB)
```json
{
  "monday": {"open": "09:00", "close": "18:00"},
  "tuesday": {"open": "09:00", "close": "18:00"},
  "wednesday": {"open": "09:00", "close": "18:00"},
  "thursday": {"open": "09:00", "close": "18:00"},
  "friday": {"open": "09:00", "close": "18:00"},
  "saturday": {"open": "10:00", "close": "20:00"},
  "sunday": {"open": "10:00", "close": "20:00"},
  "note": "週一公休"
}
```

### itinerary_data (JSONB)
```json
{
  "days": [
    {
      "day": 1,
      "theme": "台南古蹟巡禮",
      "items": [
        {
          "time": "09:00",
          "type": "sight",
          "id": 123,
          "name": "赤崁樓",
          "duration": 60
        },
        {
          "time": "11:00",
          "type": "food",
          "id": 456,
          "name": "度小月擔仔麵",
          "duration": 45
        }
      ]
    }
  ],
  "budget_breakdown": {
    "transportation": 1000,
    "food": 2000,
    "tickets": 500,
    "accommodation": 3000
  },
  "tips": ["建議穿舒適的鞋子", "夏天記得防曬"]
}
```

## 遷移策略

1. 建立新資料表（不影響現有 sights 表）
2. 擴展現有 sights 表（新增欄位）
3. 資料遷移腳本
4. 建立必要的索引
5. 設定外鍵約束

## 注意事項

- 使用 JSONB 儲存彈性資料（營業時間、行程等）
- 使用陣列儲存標籤和多張照片
- 使用 GIN 索引加速陣列查詢
- 經緯度精度設為 (10,8) 和 (11,8) 符合 GPS 精度
- 所有時間戳記使用 TIMESTAMP
- 考慮資料來源追蹤（source_url）和驗證狀態
