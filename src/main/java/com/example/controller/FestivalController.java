package com.example.controller;

import com.example.crawler.TaiwanNetFestivalCrawler;
import com.example.entity.Festival;
import com.example.repository.FestivalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/festivals")
public class FestivalController {

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private TaiwanNetFestivalCrawler crawler;

    /* ========== 爬蟲相關 API ========== */

    /**
     * 爬取台灣觀光局活動列表（支援分頁）
     * POST /festivals/crawler/all?maxPages=5&fetchDetails=true
     *
     * @param maxPages 最多爬取幾頁（0=全部）
     * @param fetchDetails 是否爬取詳細頁面（預設 false，只爬列表頁）
     */
    @PostMapping("/crawler/all")
    public ResponseEntity<Map<String, Object>> crawlAllFestivals(
            @RequestParam(defaultValue = "0") int maxPages,
            @RequestParam(defaultValue = "false") boolean fetchDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 爬取活動列表
            List<Festival> festivals = crawler.crawlAllPages(maxPages, fetchDetails);

            if (festivals.isEmpty()) {
                response.put("success", false);
                response.put("message", "未爬取到任何活動");
                return ResponseEntity.ok(response);
            }

            // 批次儲存
            List<Festival> savedFestivals = crawler.saveOrUpdateBatch(festivals);

            response.put("success", true);
            response.put("totalCrawled", festivals.size());
            response.put("totalSaved", savedFestivals.size());
            response.put("fetchDetails", fetchDetails);
            response.put("festivals", savedFestivals);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "爬取失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 爬取單一活動詳細資訊
     * POST /festivals/crawler/url
     * Body: { "url": "https://www.taiwan.net.tw/m1.aspx?sNo=0001019&lid=081434" }
     */
    @PostMapping("/crawler/url")
    public ResponseEntity<Map<String, Object>> crawlSingleUrl(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            response.put("success", false);
            response.put("message", "請提供 URL");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Festival festival = crawler.crawlDetailPage(url);

            if (festival != null) {
                Festival saved = crawler.saveOrUpdate(festival);
                response.put("success", true);
                response.put("festival", saved);
            } else {
                response.put("success", false);
                response.put("message", "爬取失敗");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /* ========== 節慶 CRUD API ========== */

    /**
     * 查詢節慶活動（統一入口，支援多條件過濾）
     *
     * 範例：
     * GET /festivals                                    → 所有活動（預設前20筆）
     * GET /festivals?status=ongoing                     → 進行中的活動
     * GET /festivals?status=upcoming&days=30            → 未來30天的活動
     * GET /festivals?keyword=台北                        → 名稱包含「台北」的活動
     * GET /festivals?zone=信義區                         → 信義區的活動
     * GET /festivals?keyword=燈會&status=ongoing        → 名稱包含「燈會」且進行中的活動
     * GET /festivals?limit=50&offset=0                  → 分頁查詢
     *
     * 查詢邏輯：
     * - status: ongoing（進行中）、upcoming（即將開始）、all（全部，預設值）
     * - keyword: 節慶名稱模糊搜尋
     * - zone: 區域過濾
     * - days: 未來幾天內的活動（僅用於 upcoming，預設30天）
     * - limit: 返回筆數上限（預設20，最大100）
     * - offset: 分頁偏移（預設0）
     */
    @GetMapping
    public ResponseEntity<List<Festival>> getFestivals(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String zone,
            @RequestParam(defaultValue = "30") Integer days,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        // 限制 limit 最大值為 100
        if (limit > 100) {
            limit = 100;
        }

        List<Festival> allFestivals;

        // 根據 status 篩選
        if ("ongoing".equals(status)) {
            allFestivals = festivalRepository.findOngoingFestivals(LocalDate.now());
        } else if ("upcoming".equals(status)) {
            LocalDate now = LocalDate.now();
            LocalDate future = now.plusDays(days);
            allFestivals = festivalRepository.findUpcomingFestivals(now, future);
        } else {
            allFestivals = festivalRepository.findAll();
        }

        // 根據 keyword 篩選
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            allFestivals = allFestivals.stream()
                    .filter(f -> f.getFestivalName() != null &&
                                 f.getFestivalName().toLowerCase().contains(lowerKeyword))
                    .toList();
        }

        // 根據 zone 篩選
        if (zone != null && !zone.trim().isEmpty()) {
            allFestivals = allFestivals.stream()
                    .filter(f -> zone.equals(f.getZone()))
                    .toList();
        }

        // 分頁處理
        int totalSize = allFestivals.size();
        int fromIndex = Math.min(offset, totalSize);
        int toIndex = Math.min(offset + limit, totalSize);

        List<Festival> paginatedFestivals = allFestivals.subList(fromIndex, toIndex);

        return ResponseEntity.ok(paginatedFestivals);
    }

    /**
     * 根據 ID 取得單一節慶詳細資訊
     * GET /festivals/123
     */
    @GetMapping("/{id}")
    public ResponseEntity<Festival> getFestivalById(@PathVariable Long id) {
        Festival festival = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到活動：ID = " + id));
        return ResponseEntity.ok(festival);
    }

    /**
     * 新增節慶
     * POST /festivals
     */
    @PostMapping
    public ResponseEntity<Festival> createFestival(@RequestBody Festival festival) {
        Festival saved = festivalRepository.save(festival);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新節慶
     * PUT /festivals/1
     */
    @PutMapping("/{id}")
    public ResponseEntity<Festival> updateFestival(@PathVariable Long id, @RequestBody Festival festival) {
        Festival existing = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到活動：ID = " + id));

        festival.setId(id);
        festival.setCreatedAt(existing.getCreatedAt());
        Festival updated = festivalRepository.save(festival);

        return ResponseEntity.ok(updated);
    }

    /**
     * 刪除節慶
     * DELETE /festivals/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFestival(@PathVariable Long id) {
        festivalRepository.deleteById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "活動已刪除");

        return ResponseEntity.ok(response);
    }
}
