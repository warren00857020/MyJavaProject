package com.example.controller;

import com.example.crawler.CrawlerService;
import com.example.crawler.CrawlerService.CrawlResult;
import com.example.crawler.TravelKingSightCrawler;
import com.example.entity.*;
import com.example.parameter.SightQueryParameter;
import com.example.service.SightService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 景點控制器
 * 提供景點 CRUD 操作及爬蟲功能
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class SightController {

    @Autowired
    private SightService sightService;

    @Autowired
    private TravelKingSightCrawler crawler;

    @Autowired
    private CrawlerService crawlerService;

    /* ========== 爬蟲相關 API ========== */

    /**
     * 爬取指定城市的景點（保留舊版 API）
     * GET /setSights/{city}
     * @deprecated 建議改用 POST /sights/crawler/region?region=TPE
     */
    @GetMapping("/setSights/{city}")
    @Deprecated
    public ResponseEntity<List<Sight>> putSights(@PathVariable("city") String city) throws IOException {
        List<Sight> sights = crawler.crawlByCity(city);
        return ResponseEntity.ok().body(sights);
    }

    /**
     * 爬取單一縣市的景點
     * POST /sights/crawler/region?region=TPE
     */
    @PostMapping("/sights/crawler/region")
    public ResponseEntity<CrawlResult> crawlSightsByRegion(@RequestParam String region) {
        CrawlResult result = crawlerService.crawlSightsByRegion(region);
        return ResponseEntity.ok(result);
    }

    /**
     * 爬取所有縣市的景點
     * POST /sights/crawler/all
     */
    @PostMapping("/sights/crawler/all")
    public ResponseEntity<Map<String, Object>> crawlAllSights() {
        List<CrawlResult> results = crawlerService.crawlAllSights();

        long successCount = results.stream().filter(CrawlResult::isSuccess).count();
        int totalItems = results.stream().mapToInt(CrawlResult::getItemsProcessed).sum();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "完成爬取");
        response.put("totalRegions", results.size());
        response.put("successRegions", successCount);
        response.put("totalItems", totalItems);
        response.put("details", results);

        return ResponseEntity.ok(response);
    }

    /**
     * 爬取多個指定縣市的景點
     * POST /sights/crawler/batch
     * Body: { "regions": ["TPE", "TXG", "KHH"] }
     */
    @PostMapping("/sights/crawler/batch")
    public ResponseEntity<Map<String, Object>> crawlMultipleRegions(@RequestBody Map<String, List<String>> request) {
        List<String> regions = request.get("regions");

        if (regions == null || regions.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "請提供要爬取的縣市列表");
            return ResponseEntity.badRequest().body(error);
        }

        List<CrawlResult> results = crawlerService.crawlMultipleRegions(regions);

        long successCount = results.stream().filter(CrawlResult::isSuccess).count();
        int totalItems = results.stream().mapToInt(CrawlResult::getItemsProcessed).sum();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "完成批次爬取");
        response.put("totalRegions", results.size());
        response.put("successRegions", successCount);
        response.put("totalItems", totalItems);
        response.put("details", results);

        return ResponseEntity.ok(response);
    }

    /**
     * 直接爬取指定 URL 的景點
     * POST /sights/crawler/url
     * Body: { "url": "https://www.travelking.com.tw/tourguide/scenery1397.html" }
     */
    @PostMapping("/sights/crawler/url")
    public ResponseEntity<Map<String, Object>> crawlSingleUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        if (url == null || url.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "請提供景點 URL");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> response = new HashMap<>();

        try {
            Sight sight = crawler.crawlSingleUrl(url);

            if (sight != null) {
                response.put("success", true);
                response.put("sight", sight);
                response.put("message", "成功爬取景點");
            } else {
                response.put("success", false);
                response.put("message", "無法爬取該 URL");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 批次爬取 urls.json.txt 中的所有景點 URL
     * POST /sights/crawler/urls?batchSize=10&randomize=true
     *
     * 優化防擋機制（已強化）：
     * - 支援批次爬取（預設每批10筆，避免被擋）
     * - 智能隨機延遲：70% 正常(5-12秒)、20% 暫停(15-30秒)、10% 長停(30-60秒)
     * - 支援 URL 隨機化爬取（randomize=true）
     * - 自動跳過 done.txt 中已完成的 URL
     * - 成功後自動記錄到 done.txt
     * - 失敗自動重試5次（指數退避）
     */
    @PostMapping("/sights/crawler/urls")
    public ResponseEntity<Map<String, Object>> crawlMultipleUrls(
            @RequestParam(defaultValue = "10") int batchSize,
            @RequestParam(defaultValue = "true") boolean randomize) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 讀取 urls.json.txt
            org.springframework.core.io.ClassPathResource resource =
                new org.springframework.core.io.ClassPathResource("urls.json.txt");

            String content = new String(
                resource.getInputStream().readAllBytes(),
                java.nio.charset.StandardCharsets.UTF_8
            );

            org.json.JSONObject jsonObject = new org.json.JSONObject(content);
            org.json.JSONArray urlsArray = jsonObject.getJSONArray("urls");

            List<String> allUrls = new ArrayList<>();
            for (int i = 0; i < urlsArray.length(); i++) {
                allUrls.add(urlsArray.getString(i));
            }

            if (allUrls.isEmpty()) {
                response.put("success", false);
                response.put("message", "urls.json.txt 中沒有找到 URL");
                return ResponseEntity.badRequest().body(response);
            }

            // 2. 讀取 done.txt（已完成的 URL）
            java.util.Set<String> doneUrls = new java.util.HashSet<>();
            try {
                java.nio.file.Path donePath = java.nio.file.Paths.get("src/main/resources/done.txt");
                if (java.nio.file.Files.exists(donePath)) {
                    List<String> doneLines = java.nio.file.Files.readAllLines(donePath);
                    doneUrls.addAll(doneLines);
                    System.out.println(String.format("📋 已讀取 %d 筆已完成的 URL", doneUrls.size()));
                }
            } catch (Exception e) {
                System.out.println("⚠️ 讀取 done.txt 失敗，將視為無已完成記錄：" + e.getMessage());
            }

            // 3. 過濾掉已完成的 URL
            List<String> pendingUrls = allUrls.stream()
                .filter(url -> !doneUrls.contains(url))
                .collect(java.util.stream.Collectors.toList());

            if (pendingUrls.isEmpty()) {
                response.put("success", true);
                response.put("message", "🎉 所有 URL 都已爬取完成！");
                response.put("totalUrls", allUrls.size());
                response.put("doneCount", doneUrls.size());
                return ResponseEntity.ok(response);
            }

            System.out.println(String.format("📊 總共 %d 筆 URL，已完成 %d 筆，待爬取 %d 筆",
                allUrls.size(), doneUrls.size(), pendingUrls.size()));

            // 4. 隨機化 URL 順序（如果啟用）
            if (randomize) {
                java.util.Collections.shuffle(pendingUrls);
                System.out.println("🔀 已隨機打亂 URL 順序");
            }

            // 5. 取本批次要爬取的 URL
            int actualBatchSize = Math.min(batchSize, pendingUrls.size());
            List<String> batchUrls = pendingUrls.subList(0, actualBatchSize);

            System.out.println(String.format("🎯 本批次爬取 %d 筆", batchUrls.size()));

            // 6. 開始批次爬取
            List<Sight> successSights = new ArrayList<>();
            List<String> failedUrls = new ArrayList<>();
            List<String> successUrls = new ArrayList<>();  // 記錄成功的 URL

            for (int i = 0; i < batchUrls.size(); i++) {
                String url = batchUrls.get(i);

                try {
                    Sight sight = crawler.crawlSingleUrl(url);

                    if (sight != null) {
                        successSights.add(sight);
                        successUrls.add(url);
                        System.out.println(String.format("✅ [%d/%d] 成功：%s",
                            i + 1, batchUrls.size(), sight.getSightName()));

                        // 立即寫入 done.txt
                        appendToDoneFile(url);

                    } else {
                        failedUrls.add(url);
                        System.out.println(String.format("❌ [%d/%d] 失敗：%s",
                            i + 1, batchUrls.size(), url));
                    }

                    // 智能隨機延遲（使用新的延遲策略）
                    if (i < batchUrls.size() - 1) {  // 最後一筆不需要延遲
                        long delay = com.example.crawler.config.CrawlerConfig.getRandomDelay();
                        System.out.println(String.format("⏳ 延遲 %.1f 秒...", delay / 1000.0));
                        Thread.sleep(delay);
                    }

                } catch (Exception e) {
                    failedUrls.add(url + " (錯誤: " + e.getMessage() + ")");
                    System.out.println(String.format("⚠️ [%d/%d] 異常：%s",
                        i + 1, batchUrls.size(), e.getMessage()));
                }
            }

            // 7. 計算進度
            int newDoneCount = doneUrls.size() + successUrls.size();
            int remainingCount = allUrls.size() - newDoneCount;
            boolean hasMore = remainingCount > 0;

            response.put("success", true);
            response.put("totalUrls", allUrls.size());
            response.put("previousDoneCount", doneUrls.size());
            response.put("newDoneCount", successUrls.size());
            response.put("totalDoneCount", newDoneCount);
            response.put("remainingCount", remainingCount);
            response.put("failedCount", failedUrls.size());
            response.put("batchSize", batchUrls.size());
            response.put("hasMore", hasMore);
            response.put("successSights", successSights);
            response.put("failedUrls", failedUrls);

            // 8. 批次完成訊息
            if (hasMore) {
                System.out.println(String.format("\n⏸️ 本批次完成！還有 %d 筆待爬取", remainingCount));
                response.put("message", String.format(
                    "✅ 本批次完成！成功 %d 筆，失敗 %d 筆。\n" +
                    "📊 總進度：%d/%d (%.1f%%)\n" +
                    "⚠️ 建議休息 10-30 秒後再次調用相同 API",
                    successUrls.size(), failedUrls.size(),
                    newDoneCount, allUrls.size(),
                    (newDoneCount * 100.0 / allUrls.size())));
            } else {
                System.out.println("\n🎉 全部爬取完成！");
                response.put("message", String.format(
                    "🎉 全部爬取完成！\n" +
                    "✅ 成功：%d 筆\n" +
                    "❌ 失敗：%d 筆",
                    newDoneCount, allUrls.size() - newDoneCount));
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "爬取失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 輔助方法：將成功的 URL 追加到 done.txt
     */
    private void appendToDoneFile(String url) {
        try {
            java.nio.file.Path donePath = java.nio.file.Paths.get("src/main/resources/done.txt");

            // 確保目錄存在
            java.nio.file.Files.createDirectories(donePath.getParent());

            // 追加寫入
            java.nio.file.Files.write(
                donePath,
                (url + System.lineSeparator()).getBytes(),
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND
            );

        } catch (Exception e) {
            System.err.println("⚠️ 寫入 done.txt 失敗：" + e.getMessage());
        }
    }

    /* ========== 景點 CRUD API ========== */

    //查詢景點
    @GetMapping("/sights/{sightName}")
    public ResponseEntity<SightResponse> getSight(@PathVariable("sightName") String name){
        SightResponse sight = sightService.getSight(name);

        return ResponseEntity.ok(sight);
    }

    /**
     * 查詢景點（支援多條件 AND 交集查詢）
     *
     * 範例：
     * GET /sights                                    → 返回所有景點（預設前20筆）
     * GET /sights?city=台北市                         → 地址包含「台北市」的景點
     * GET /sights?zone=信義區                         → 區域為「信義區」的景點
     * GET /sights?keyword=博物館                      → 名稱/描述/地址/標籤包含「博物館」的景點
     * GET /sights?city=台北市&zone=信義區              → 台北市 AND 信義區 (交集)
     * GET /sights?city=台北市&zone=信義區&keyword=博物館 → 台北市 AND 信義區 AND 博物館 (交集)
     * GET /sights?limit=50&offset=0                  → 分頁查詢
     *
     * 查詢邏輯：
     * - 所有條件使用 AND 連接（取交集）
     * - city: 地址模糊匹配（LIKE %台北市%）
     * - zone: 區域精確匹配（= 信義區）
     * - keyword: 名稱/描述/地址/標籤模糊匹配（OR）
     * - 未提供的參數會被忽略
     * - limit: 返回筆數上限（預設20，最大100）
     * - offset: 分頁偏移（預設0）
     */
    @GetMapping("/sights")
    public ResponseEntity<List<SightResponse>> getSights(
            @ModelAttribute SightQueryParameter param,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset){

        // 限制 limit 最大值為 100
        if (limit > 100) {
            limit = 100;
        }

        // 統一使用多條件動態查詢
        List<SightResponse> allSights = sightService.searchByMultipleConditions(
            param.getCity(),
            param.getZone(),
            param.getKeyword()
        );

        // 分頁處理
        int totalSize = allSights.size();
        int fromIndex = Math.min(offset, totalSize);
        int toIndex = Math.min(offset + limit, totalSize);

        List<SightResponse> paginatedSights = allSights.subList(fromIndex, toIndex);

        return ResponseEntity.ok(paginatedSights);
    }

    //刪除景點
    @DeleteMapping("/sights/{sightName}")
    public ResponseEntity<Void> deleteSight(@PathVariable("sightName") String name) {
        sightService.deleteSight(name);

        return ResponseEntity.noContent().build();
    }


}
