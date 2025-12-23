package com.example.controller;

import com.example.crawler.CrawlerService;
import com.example.crawler.CrawlerService.CrawlResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 爬蟲 Controller
 * 提供手動觸發爬蟲的 API 端點
 */
@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;

    /**
     * 爬取單一縣市的景點
     * GET /api/crawler/sights?region=TPE
     */
    @GetMapping("/sights")
    public ResponseEntity<CrawlResult> crawlSightsByRegion(@RequestParam String region) {
        CrawlResult result = crawlerService.crawlSightsByRegion(region);
        return ResponseEntity.ok(result);
    }

    /**
     * 爬取所有縣市的景點
     * POST /api/crawler/sights/all
     */
    @PostMapping("/sights/all")
    public ResponseEntity<Map<String, Object>> crawlAllSights() {
        List<CrawlResult> results = crawlerService.crawlAllSights();

        // 統計結果
        long successCount = results.stream().filter(CrawlResult::isSuccess).count();
        int totalItems = results.stream().mapToInt(CrawlResult::getItemsProcessed).sum();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "完成爬取",
            "totalRegions", results.size(),
            "successRegions", successCount,
            "totalItems", totalItems,
            "details", results
        ));
    }

    /**
     * 爬取多個指定縣市的景點
     * POST /api/crawler/sights/batch
     * Body: { "regions": ["TPE", "TXG", "KHH"] }
     */
    @PostMapping("/sights/batch")
    public ResponseEntity<Map<String, Object>> crawlMultipleRegions(@RequestBody Map<String, List<String>> request) {
        List<String> regions = request.get("regions");

        if (regions == null || regions.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "請提供要爬取的縣市列表"
            ));
        }

        List<CrawlResult> results = crawlerService.crawlMultipleRegions(regions);

        long successCount = results.stream().filter(CrawlResult::isSuccess).count();
        int totalItems = results.stream().mapToInt(CrawlResult::getItemsProcessed).sum();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "完成批次爬取",
            "totalRegions", results.size(),
            "successRegions", successCount,
            "totalItems", totalItems,
            "details", results
        ));
    }

    /**
     * 健康檢查
     * GET /api/crawler/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "service", "crawler",
            "message", "爬蟲服務正常運行"
        ));
    }
}
