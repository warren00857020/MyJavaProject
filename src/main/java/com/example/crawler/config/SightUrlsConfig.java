package com.example.crawler.config;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 景點 URL 配置管理器
 * 讀取並管理 taipei-sights-urls.json 中的景點 URL
 */
@Component
public class SightUrlsConfig {

    private static final Logger logger = LoggerFactory.getLogger(SightUrlsConfig.class);

    private static final String CONFIG_FILE = "taipei-sights-urls.json";

    private Map<String, List<String>> urlsByZone = new HashMap<>();
    private String cityName;
    private String cityCode;
    private int totalUrls = 0;

    @PostConstruct
    public void loadConfig() {
        try {
            logger.info("正在載入景點 URL 配置: {}", CONFIG_FILE);

            ClassPathResource resource = new ClassPathResource(CONFIG_FILE);
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            JSONObject config = new JSONObject(content);

            // 讀取城市資訊
            cityName = config.getString("city");
            cityCode = config.getString("cityCode");

            // 讀取區域 URL 對應
            JSONObject regions = config.getJSONObject("regions");

            for (String zone : regions.keySet()) {
                JSONObject regionData = regions.getJSONObject(zone);
                List<String> urls = new ArrayList<>();

                regionData.getJSONArray("urls").forEach(url -> {
                    urls.add(url.toString());
                    totalUrls++;
                });

                urlsByZone.put(zone, urls);
            }

            logger.info("✅ 成功載入景點 URL 配置：{} ({}) - {} 個行政區，總共 {} 個景點",
                    cityName, cityCode, urlsByZone.size(), totalUrls);

            // 顯示每個區的統計
            urlsByZone.forEach((zone, urls) -> {
                logger.info("  📍 {} - {} 個景點", zone, urls.size());
            });

        } catch (IOException e) {
            logger.error("載入景點 URL 配置失敗：{}", e.getMessage());
            // 使用空配置
            urlsByZone = new HashMap<>();
        }
    }

    /**
     * 獲取指定行政區的所有景點 URL
     */
    public List<String> getUrlsByZone(String zone) {
        return urlsByZone.getOrDefault(zone, new ArrayList<>());
    }

    /**
     * 獲取所有行政區名稱
     */
    public Set<String> getAllZones() {
        return urlsByZone.keySet();
    }

    /**
     * 獲取所有景點 URL（不區分行政區）
     */
    public List<String> getAllUrls() {
        List<String> allUrls = new ArrayList<>();
        urlsByZone.values().forEach(allUrls::addAll);
        return allUrls;
    }

    /**
     * 獲取 URL 與行政區的對應表
     */
    public Map<String, String> getUrlZoneMapping() {
        Map<String, String> mapping = new HashMap<>();
        urlsByZone.forEach((zone, urls) -> {
            urls.forEach(url -> mapping.put(url, zone));
        });
        return mapping;
    }

    /**
     * 獲取指定 URL 所屬的行政區
     */
    public String getZoneByUrl(String url) {
        for (Map.Entry<String, List<String>> entry : urlsByZone.entrySet()) {
            if (entry.getValue().contains(url)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 獲取統計資訊
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cityName", cityName);
        stats.put("cityCode", cityCode);
        stats.put("totalZones", urlsByZone.size());
        stats.put("totalUrls", totalUrls);

        Map<String, Integer> zoneStats = new HashMap<>();
        urlsByZone.forEach((zone, urls) -> zoneStats.put(zone, urls.size()));
        stats.put("urlsByZone", zoneStats);

        return stats;
    }

    // Getters
    public String getCityName() {
        return cityName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public int getTotalUrls() {
        return totalUrls;
    }

    public Map<String, List<String>> getUrlsByZoneMap() {
        return new HashMap<>(urlsByZone);
    }
}
