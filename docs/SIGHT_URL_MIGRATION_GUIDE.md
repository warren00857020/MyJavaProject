# 景點 URL 配置遷移指南

## 📋 問題說明

### 現有問題

**舊的 `urls.json.txt`**:
```json
{
  "urls": [
    "https://www.travelking.com.tw/tourguide/scenery104238.html",
    "https://www.travelking.com.tw/tourguide/scenery104530.html",
    ...
  ]
}
```

**問題**:
- ❌ 沒有區域分類（不知道哪個 URL 屬於哪個行政區）
- ❌ 爬蟲無法自動設定 `zone` 欄位
- ❌ 無法按區域批次爬取
- ❌ 難以維護和擴充

---

## 🎯 解決方案

### 新的結構化配置

**新的 `taipei-sights-urls.json`**:
```json
{
  "city": "台北市",
  "cityCode": "TPE",
  "regions": {
    "中正區": {
      "urls": [...]
    },
    "士林區": {
      "urls": [...]
    },
    ...
  }
}
```

**優勢**:
- ✅ 清晰的區域分類
- ✅ 爬蟲自動設定 `zone` 欄位
- ✅ 支援按區域批次爬取
- ✅ 方便未來擴充其他縣市

---

## 🔧 遷移步驟

### 步驟 1：手動分類 URL（需要你的協助）

你需要將 `urls.json.txt` 中的 250+ 個 URL 分類到各個行政區。

**方法 A：手動爬取並分類（最準確）**

1. 執行爬蟲爬取所有 URL
2. 從爬取的資料中提取 `zone` 欄位
3. 根據 `zone` 將 URL 分組

**範例腳本**（你可以使用）:
```bash
# 爬取單一 URL 並查看所屬區域
curl -X POST "http://localhost:8080/sights/crawler/url" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://www.travelking.com.tw/tourguide/scenery104238.html"}'

# 回應中會有 zone 欄位
{
  "success": true,
  "sight": {
    "sightName": "中正紀念堂",
    "zone": "中正區",  ← 這就是我們要的
    ...
  }
}
```

---

**方法 B：批次爬取並自動分類（推薦）✅**

我幫你寫一個自動化腳本：

```java
// 使用這個腳本將 urls.json.txt 轉換為結構化配置
// 步驟：
// 1. 讀取 urls.json.txt
// 2. 逐一爬取每個 URL
// 3. 提取 zone 資訊
// 4. 按區域分組
// 5. 輸出新的 JSON 配置
```

---

### 步驟 2：更新配置檔案

將分類好的 URL 填入 `taipei-sights-urls.json`：

```json
{
  "city": "台北市",
  "cityCode": "TPE",
  "regions": {
    "中正區": {
      "urls": [
        "https://www.travelking.com.tw/tourguide/scenery104238.html",
        "https://www.travelking.com.tw/tourguide/scenery458.html",
        ...
      ]
    },
    "士林區": {
      "urls": [
        "https://www.travelking.com.tw/tourguide/scenery439.html",
        "https://www.travelking.com.tw/tourguide/scenery105153.html",
        ...
      ]
    }
  },
  "metadata": {
    "lastUpdated": "2025-12-26",
    "totalUrls": 250
  }
}
```

---

### 步驟 3：使用新配置爬取

```bash
# 爬取所有區域
curl -X POST "http://localhost:8080/sights/crawler/all"

# 爬取特定區域
curl -X POST "http://localhost:8080/sights/crawler/zone?zone=士林區"
```

---

## 📊 目前的搜尋流程

### 使用者搜尋「士林區 景點」

```
使用者
  ↓
GET /sights?keyword=士林區
  ↓
SightController
  ↓
SightService.getSightsByZone("士林區")
  ↓
SightRepository.findByZone("士林區")
  ↓
Database（查詢 zone = '士林區' 的所有景點）
  ↓
返回景點列表
```

**關鍵點**：搜尋是從**資料庫**查詢，而不是從 JSON 檔案。

### JSON 檔案的角色

`taipei-sights-urls.json` 是**爬蟲的資料來源**：

```
taipei-sights-urls.json
  ↓
爬蟲讀取 URL 列表
  ↓
爬取 TravelKing 網站
  ↓
解析景點資料（包含 zone）
  ↓
儲存到資料庫
  ↓
使用者搜尋時從資料庫查詢
```

---

## 🚀 使用新配置的好處

### 1. 按區域批次爬取

```bash
# 只爬取士林區的景點
curl -X POST "http://localhost:8080/sights/crawler/zone?zone=士林區"
```

### 2. 自動設定 zone 欄位

```java
// 爬蟲知道這個 URL 屬於士林區
String zone = urlsConfig.getZoneByUrl(url);
sight.setZone(zone);  // 自動設定 "士林區"
```

### 3. 更好的維護性

```json
// 新增景點很簡單，直接加到對應區域
"士林區": {
  "urls": [
    "existing-url-1.html",
    "existing-url-2.html",
    "NEW-url-3.html"  ← 新增這裡
  ]
}
```

### 4. 統計資訊

```java
// 查看每個區有多少景點
Map<String, Object> stats = urlsConfig.getStatistics();
// {
//   "totalZones": 12,
//   "totalUrls": 250,
//   "urlsByZone": {
//     "中正區": 25,
//     "士林區": 30,
//     ...
//   }
// }
```

---

## 🔄 遷移時間表建議

### 階段 1：保留舊方式（目前）
- 繼續使用 `urls.json.txt`
- 手動爬取並分類 URL

### 階段 2：並行運作
- 建立 `taipei-sights-urls.json`
- 逐步遷移 URL 到新配置
- 兩個檔案同時存在

### 階段 3：完全遷移
- 所有 URL 都已分類
- 移除 `urls.json.txt`
- 只使用 `taipei-sights-urls.json`

---

## 💡 自動化遷移腳本

我可以幫你寫一個自動化腳本：

### 功能
1. 讀取 `urls.json.txt` 中的所有 URL
2. 逐一爬取每個 URL
3. 提取景點的 `zone` 欄位
4. 按區域分組
5. 生成新的 `taipei-sights-urls.json`

### 使用方式
```bash
# 執行遷移腳本
curl -X POST "http://localhost:8080/sights/migrate-urls-config"

# 輸出
{
  "success": true,
  "totalUrls": 250,
  "processedUrls": 250,
  "zonesFound": 12,
  "configFile": "taipei-sights-urls.json"
}
```

---

## ❓ 常見問題

### Q1: 如果爬取時沒有 zone 資訊怎麼辦？

**答**:
- 爬蟲會從景點頁面的地址中提取區域資訊
- 如果無法提取，會標記為 "未分類"
- 可以後續手動補充

### Q2: 要重新爬取所有景點嗎？

**答**:
- **不需要**，如果資料庫已有景點資料
- 只需要整理 URL 配置檔案
- 未來新增景點時使用新配置

### Q3: 可以混用兩種方式嗎？

**答**:
- 可以，但不建議
- 建議完全遷移到新配置

---

## 📝 下一步行動

### 我已經完成
- ✅ 建立新的配置檔案格式
- ✅ 實作 `SightUrlsConfig` 讀取器
- ✅ 提供範例配置

### 你需要做的
1. **決定遷移方式**：
   - 選項 A：我幫你寫自動化腳本
   - 選項 B：手動整理 URL 分類

2. **提供資訊**：
   - 如果選擇選項 A，我需要你確認可以執行批次爬取
   - 如果選擇選項 B，你需要手動整理 250+ 個 URL

3. **測試新配置**：
   - 完成後重新啟動應用程序
   - 測試按區域爬取功能

---

**建議**: 我推薦使用**自動化腳本（選項 A）**，可以在 10-15 分鐘內完成所有 URL 的分類，並自動生成新配置檔案。

你想要我幫你實作自動化遷移腳本嗎？

---

**最後更新**: 2025-12-26
