package com.example.scheduler;

import com.example.entity.Food;
import com.example.entity.Region;
import com.example.entity.Sight;
import com.example.repository.RegionRepository;
import com.example.repository.SightRepository;
import com.example.service.GooglePlacesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 餐廳資料定期更新排程器
 *
 * 執行策略：
 * 1. 每週日凌晨 2:00 執行一次完整更新
 * 2. 按區域分批收集餐廳資料，避免 API 配額問題
 * 3. 優先收集熱門景點周邊餐廳
 */
@Component
public class FoodDataScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FoodDataScheduler.class);

    @Autowired
    private GooglePlacesService googlePlacesService;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private SightRepository sightRepository;

    // 台北市主要行政區列表
    private static final List<String> TAIPEI_ZONES = Arrays.asList(
        "中正區", "大同區", "中山區", "松山區", "大安區",
        "萬華區", "信義區", "士林區", "北投區", "內湖區",
        "南港區", "文山區"
    );

    /**
     * 每週日凌晨 2:00 執行完整更新
     * Cron 表達式：秒 分 時 日 月 星期
     * 0 0 2 * * SUN = 每週日 2:00:00
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void weeklyFullUpdate() {
        logger.info("========== 開始每週餐廳資料完整更新 ==========");

        try {
            // 方案 1：按行政區收集
            updateByZones();

            // 方案 2：按景點周邊收集（可選）
            // updateBySights();

            logger.info("========== 每週餐廳資料更新完成 ==========");
        } catch (Exception e) {
            logger.error("餐廳資料更新失敗", e);
        }
    }

    /**
     * 手動觸發：立即執行更新
     * 可通過 API 端點調用
     */
    public void manualUpdate() {
        logger.info("========== 手動觸發餐廳資料更新 ==========");
        updateByZones();
    }

    /**
     * 策略 1：按行政區批次收集
     * 優點：覆蓋範圍廣，不受景點限制
     */
    private void updateByZones() {
        int totalSaved = 0;

        for (String zone : TAIPEI_ZONES) {
            try {
                logger.info("正在收集：台北市{} 的餐廳資料...", zone);

                // 搜尋關鍵字：「台北市XX區 餐廳」
                String query = String.format("台北市%s 餐廳", zone);
                List<Food> foods = googlePlacesService.searchByText(query, 20);

                // 設定 region_id（從資料庫查詢台北市）
                Region taipei = regionRepository.findByName("台北市")
                    .orElse(null);

                if (taipei != null) {
                    foods.forEach(food -> food.setRegionId(taipei.getId()));
                }

                // 批次儲存
                List<Food> saved = googlePlacesService.saveOrUpdateBatch(foods);
                totalSaved += saved.size();

                logger.info("✅ {} 完成：收集 {} 筆，成功儲存 {} 筆", zone, foods.size(), saved.size());

                // 延遲 2 秒，避免觸發 API 限流
                Thread.sleep(2000);

            } catch (Exception e) {
                logger.error("收集 {} 餐廳資料失敗", zone, e);
            }
        }

        logger.info("📊 按行政區更新完成：總共儲存 {} 筆餐廳資料", totalSaved);
    }

    /**
     * 策略 2：按景點周邊收集
     * 優點：餐廳與景點直接關聯，適合行程規劃
     */
    private void updateBySights() {
        int totalSaved = 0;

        // 只處理有座標的景點
        List<Sight> sights = sightRepository.findAll().stream()
            .filter(sight -> sight.getLatitude() != null && sight.getLongitude() != null)
            .toList();

        logger.info("找到 {} 個有座標的景點", sights.size());

        for (Sight sight : sights) {
            try {
                logger.info("正在收集景點「{}」周邊餐廳...", sight.getSightName());

                // 搜尋半徑 500 公尺內的餐廳
                List<Food> foods = googlePlacesService.searchNearbyRestaurants(sight, 500, 10);

                // 設定 region_id
                if (sight.getRegionId() != null) {
                    foods.forEach(food -> food.setRegionId(sight.getRegionId()));
                }

                // 批次儲存
                List<Food> saved = googlePlacesService.saveOrUpdateBatch(foods);
                totalSaved += saved.size();

                logger.info("✅ {} 完成：收集 {} 筆", sight.getSightName(), saved.size());

                // 延遲 2 秒
                Thread.sleep(2000);

            } catch (Exception e) {
                logger.error("收集景點「{}」周邊餐廳失敗", sight.getSightName(), e);
            }
        }

        logger.info("📊 按景點更新完成：總共儲存 {} 筆餐廳資料", totalSaved);
    }

    /**
     * 測試用：每 5 分鐘執行一次（僅限開發環境）
     * 正式環境請註解掉此方法
     */
    // @Scheduled(fixedRate = 300000) // 300000ms = 5分鐘
    public void testSchedule() {
        logger.info("========== 測試排程執行 ==========");

        // 只收集一個區域測試
        try {
            String query = "台北市信義區 餐廳";
            List<Food> foods = googlePlacesService.searchByText(query, 5);
            List<Food> saved = googlePlacesService.saveOrUpdateBatch(foods);

            logger.info("測試完成：收集 {} 筆，儲存 {} 筆", foods.size(), saved.size());
        } catch (Exception e) {
            logger.error("測試排程失敗", e);
        }
    }
}
