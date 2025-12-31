package com.example.controller;

import com.example.entity.Food;
import com.example.entity.Sight;
import com.example.repository.FoodRepository;
import com.example.repository.SightRepository;
import com.example.service.GooglePlacesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 美食資料 API Controller
 */
@RestController
@RequestMapping("/foods")
public class FoodController {

    @Autowired
    private GooglePlacesService googlePlacesService;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private SightRepository sightRepository;

    /**
     * 測試 Google Places API
     * GET /foods/test-api?lat=25.033&lng=121.565&radius=500
     */
    @GetMapping("/test-api")
    public ResponseEntity<Map<String, Object>> testGooglePlacesApi(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "500") int radius,
            @RequestParam(defaultValue = "10") int maxResults) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Food> foods = googlePlacesService.searchNearby(lat, lng, radius, "restaurant", maxResults);

            response.put("success", true);
            response.put("totalFound", foods.size());
            response.put("foods", foods);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 搜尋景點周邊餐廳並儲存
     * POST /foods/crawl-by-sight/{sightId}?radius=500&maxResults=20
     */
    @PostMapping("/crawl-by-sight/{sightId}")
    public ResponseEntity<Map<String, Object>> crawlBySight(
            @PathVariable Long sightId,
            @RequestParam(defaultValue = "500") int radius,
            @RequestParam(defaultValue = "20") int maxResults) {

        Map<String, Object> response = new HashMap<>();

        try {
            Sight sight = sightRepository.findById(sightId)
                    .orElseThrow(() -> new RuntimeException("景點不存在：" + sightId));

            // 搜尋餐廳
            List<Food> foods = googlePlacesService.searchNearbyRestaurants(sight, radius, maxResults);

            // 設定 regionId（從景點繼承）
            for (Food food : foods) {
                if (food.getRegionId() == null && sight.getRegionId() != null) {
                    food.setRegionId(sight.getRegionId());
                }
            }

            // 儲存到資料庫
            List<Food> savedFoods = googlePlacesService.saveOrUpdateBatch(foods);

            response.put("success", true);
            response.put("sightName", sight.getSightName());
            response.put("totalFound", foods.size());
            response.put("totalSaved", savedFoods.size());
            response.put("foods", savedFoods);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 文字搜尋並儲存餐廳資料
     * POST /foods/crawl-by-text?query=台北市信義區 餐廳&maxResults=20&enrichDetails=false
     *
     * @param query 搜尋關鍵字（例：台北市信義區 餐廳）
     * @param maxResults 返回筆數上限（預設 20）
     * @param enrichDetails 是否補充詳細資訊（預設 false）
     *   - false: 只抓取基本資訊（快速，Text Search API）
     *   - true: 每筆都補充詳細資訊（慢，Text Search + Place Details API）
     *
     * 注意：enrichDetails=true 會為每筆餐廳額外呼叫一次 Place Details API，
     *      請注意 Google API 配額限制，建議小批量使用或在非尖峰時段執行
     */
    @PostMapping("/crawl-by-text")
    public ResponseEntity<Map<String, Object>> crawlByText(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int maxResults,
            @RequestParam(defaultValue = "false") boolean enrichDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 搜尋餐廳（基本資訊）
            List<Food> foods = googlePlacesService.searchByText(query, maxResults);

            if (foods.isEmpty()) {
                response.put("success", true);
                response.put("query", query);
                response.put("totalFound", 0);
                response.put("totalSaved", 0);
                response.put("message", "沒有找到符合條件的餐廳");
                response.put("foods", foods);
                return ResponseEntity.ok(response);
            }

            // 2. 儲存基本資訊到資料庫
            List<Food> savedFoods = googlePlacesService.saveOrUpdateBatch(foods);

            // 3. 如果需要補充詳細資訊
            int enrichedCount = 0;
            int enrichFailedCount = 0;

            if (enrichDetails) {
                System.out.println(String.format("🔍 開始補充 %d 筆餐廳的詳細資訊...", savedFoods.size()));

                for (int i = 0; i < savedFoods.size(); i++) {
                    Food food = savedFoods.get(i);

                    try {
                        // 補充詳細資訊
                        Food enriched = googlePlacesService.enrichWithDetails(food);
                        foodRepository.save(enriched);
                        enrichedCount++;

                        System.out.println(String.format("✅ [%d/%d] 已補充：%s",
                            i + 1, savedFoods.size(), food.getFoodName()));

                        // 延遲 1 秒，避免觸發 API 限流
                        if (i < savedFoods.size() - 1) {
                            Thread.sleep(1000);
                        }

                    } catch (Exception e) {
                        enrichFailedCount++;
                        System.err.println(String.format("⚠️ [%d/%d] 補充失敗：%s - %s",
                            i + 1, savedFoods.size(), food.getFoodName(), e.getMessage()));
                    }
                }

                response.put("enrichDetails", true);
                response.put("enrichedCount", enrichedCount);
                response.put("enrichFailedCount", enrichFailedCount);
            } else {
                response.put("enrichDetails", false);
                response.put("message", "僅爬取基本資訊。如需完整營業時間、電話、網站，請使用 enrichDetails=true 或稍後調用 /foods/enrich-all");
            }

            response.put("success", true);
            response.put("query", query);
            response.put("totalFound", foods.size());
            response.put("totalSaved", savedFoods.size());
            response.put("foods", savedFoods);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 查詢美食餐廳（統一入口，支援多條件過濾）
     *
     * 範例：
     * GET /foods                                → 所有餐廳（預設前20筆）
     * GET /foods?keyword=火鍋                    → 名稱包含「火鍋」的餐廳
     * GET /foods?zone=信義區                     → 信義區的餐廳
     * GET /foods?keyword=咖啡&zone=大安區        → 大安區的咖啡廳
     * GET /foods?limit=50&offset=0              → 分頁查詢
     *
     * 查詢邏輯：
     * - keyword: 餐廳名稱、地址模糊搜尋
     * - zone: 行政區過濾
     * - limit: 返回筆數上限（預設20，最大100）
     * - offset: 分頁偏移（預設0）
     */
    @GetMapping
    public ResponseEntity<List<Food>> getFoods(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String zone,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        // 限制 limit 最大值為 100
        if (limit > 100) {
            limit = 100;
        }

        List<Food> allFoods;

        // 根據條件查詢
        if (keyword != null && !keyword.trim().isEmpty()) {
            allFoods = foodRepository.searchByKeyword(keyword);
        } else if (zone != null && !zone.trim().isEmpty()) {
            allFoods = foodRepository.findByZone(zone);
        } else {
            allFoods = foodRepository.findAll();
        }

        // 如果同時有 keyword 和 zone，進行二次過濾
        if (keyword != null && !keyword.trim().isEmpty() &&
            zone != null && !zone.trim().isEmpty()) {
            String finalZone = zone;
            allFoods = allFoods.stream()
                    .filter(f -> finalZone.equals(f.getZone()))
                    .toList();
        }

        // 分頁處理
        int totalSize = allFoods.size();
        int fromIndex = Math.min(offset, totalSize);
        int toIndex = Math.min(offset + limit, totalSize);

        List<Food> paginatedFoods = allFoods.subList(fromIndex, toIndex);

        return ResponseEntity.ok(paginatedFoods);
    }

    /**
     * 獲取單一餐廳的詳細資訊（包含完整營業時間）
     * GET /foods/{id}/details
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getFoodDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Food food = foodRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("餐廳不存在：" + id));

            if (food.getPlaceId() == null) {
                response.put("success", false);
                response.put("error", "此餐廳沒有 Google Place ID，無法獲取詳細資訊");
                return ResponseEntity.ok(response);
            }

            // 獲取詳細資訊
            Food detailedFood = googlePlacesService.getPlaceDetails(food.getPlaceId());

            if (detailedFood != null) {
                // 更新資料庫中的餐廳資訊
                food.setOpeningHours(detailedFood.getOpeningHours());
                food.setPhone(detailedFood.getPhone());
                food.setOfficialWebsite(detailedFood.getOfficialWebsite());

                Food saved = foodRepository.save(food);

                response.put("success", true);
                response.put("food", saved);
                response.put("message", "已更新完整營業時間、電話和網站資訊");
            } else {
                response.put("success", false);
                response.put("error", "無法從 Google Places API 獲取詳細資訊");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 批次補充所有餐廳的詳細資訊
     * POST /foods/enrich-all
     * 警告：這會對每個餐廳呼叫一次 Place Details API，請注意配額限制
     */
    @PostMapping("/enrich-all")
    public ResponseEntity<Map<String, Object>> enrichAllFoods(
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Food> allFoods = foodRepository.findAll();
            int total = allFoods.size();
            int processed = 0;
            int success = 0;
            int failed = 0;

            // 限制處理數量，避免一次性消耗太多配額
            int maxProcess = Math.min(limit, total);

            for (int i = 0; i < maxProcess; i++) {
                Food food = allFoods.get(i);

                try {
                    Food enriched = googlePlacesService.enrichWithDetails(food);
                    foodRepository.save(enriched);
                    success++;

                    // 延遲 1 秒，避免觸發 API 限流
                    Thread.sleep(1000);

                } catch (Exception e) {
                    failed++;
                }

                processed++;
            }

            response.put("success", true);
            response.put("total", total);
            response.put("processed", processed);
            response.put("success_count", success);
            response.put("failed_count", failed);
            response.put("message", String.format("已處理 %d/%d 筆餐廳資料", processed, total));

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
