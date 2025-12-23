package com.example.crawler;

import com.example.crawler.config.CityMappingConfig;
import com.example.entity.Region;
import com.example.entity.Sight;
import com.example.repository.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 爬蟲服務層
 * 協調和管理爬蟲作業
 */
@Service
public class CrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

    @Autowired
    private TravelKingSightCrawler sightCrawler;

    @Autowired
    private CityMappingConfig cityMappingConfig;

    @Autowired
    private RegionRepository regionRepository;

    /**
     * 爬取單一縣市的景點資料
     * @param regionCode 資料庫的 region code（例：TPE）
     * @return 爬取結果統計
     */
    public CrawlResult crawlSightsByRegion(String regionCode) {
        logger.info("開始爬取景點：regionCode={}", regionCode);

        CrawlResult result = new CrawlResult();
        result.setRegionCode(regionCode);
        result.setType("sight");
        result.setStartTime(System.currentTimeMillis());

        try {
            // 1. 獲取 Region ID
            Region region = regionRepository.findByCode(regionCode)
                .orElseThrow(() -> new RuntimeException("找不到縣市：" + regionCode));

            Long regionId = region.getId();

            // 2. 將 regionCode 轉換為 TravelKing 的 county slug
            String countySlug = cityMappingConfig.getCountySlug(regionCode);
            if (countySlug == null) {
                throw new RuntimeException("無法映射縣市代碼：" + regionCode);
            }

            // 3. 獲取該縣市下的所有城市列表
            List<String> cities = cityMappingConfig.getCities(countySlug);
            if (cities.isEmpty()) {
                throw new RuntimeException("找不到城市列表：" + countySlug);
            }

            // 4. 爬取資料
            List<Sight> sights = sightCrawler.crawlByCounty(countySlug, cities);

            // 5. 設定 regionId（因為爬蟲無法直接知道）
            for (Sight sight : sights) {
                sight.setRegionId(regionId);
            }

            // 6. 批次儲存
            if (!sights.isEmpty()) {
                sightCrawler.saveBatch(sights);
            }

            result.setSuccess(true);
            result.setItemsProcessed(sights.size());
            result.setMessage("成功爬取 " + sights.size() + " 個景點");

        } catch (Exception e) {
            logger.error("爬取失敗：{}", regionCode, e);
            result.setSuccess(false);
            result.setMessage("爬取失敗：" + e.getMessage());
        }

        result.setEndTime(System.currentTimeMillis());
        return result;
    }

    /**
     * 爬取所有縣市的景點資料
     * @return 爬取結果統計列表
     */
    public List<CrawlResult> crawlAllSights() {
        logger.info("開始爬取所有縣市的景點");

        List<CrawlResult> results = new ArrayList<>();

        // 獲取所有縣市代碼
        for (String regionCode : CityMappingConfig.REGION_TO_COUNTY_MAP.keySet()) {
            CrawlResult result = crawlSightsByRegion(regionCode);
            results.add(result);

            logger.info("完成 {} 的爬取：成功={}, 數量={}",
                regionCode, result.isSuccess(), result.getItemsProcessed());
        }

        return results;
    }

    /**
     * 爬取指定縣市列表的景點
     * @param regionCodes 縣市代碼列表
     * @return 爬取結果統計列表
     */
    public List<CrawlResult> crawlMultipleRegions(List<String> regionCodes) {
        logger.info("開始爬取指定縣市：{}", regionCodes);

        List<CrawlResult> results = new ArrayList<>();

        for (String regionCode : regionCodes) {
            CrawlResult result = crawlSightsByRegion(regionCode);
            results.add(result);
        }

        return results;
    }

    /**
     * 爬取結果統計類
     */
    public static class CrawlResult {
        private String regionCode;
        private String type;  // sight, food, festival
        private boolean success;
        private int itemsProcessed;
        private String message;
        private long startTime;
        private long endTime;

        // Getters and Setters
        public String getRegionCode() {
            return regionCode;
        }

        public void setRegionCode(String regionCode) {
            this.regionCode = regionCode;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getItemsProcessed() {
            return itemsProcessed;
        }

        public void setItemsProcessed(int itemsProcessed) {
            this.itemsProcessed = itemsProcessed;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public long getDuration() {
            return endTime - startTime;
        }
    }
}
