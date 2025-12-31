package com.example.controller;

import com.example.scheduler.FoodDataScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 排程任務控制器
 * 用於手動觸發定時任務
 */
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    @Autowired
    private FoodDataScheduler foodDataScheduler;

    /**
     * 手動觸發餐廳資料更新
     * POST /scheduler/update-foods
     */
    @PostMapping("/update-foods")
    public ResponseEntity<Map<String, Object>> triggerFoodUpdate() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 在新執行緒中執行，避免阻塞 HTTP 請求
            new Thread(() -> {
                foodDataScheduler.manualUpdate();
            }).start();

            result.put("success", true);
            result.put("message", "餐廳資料更新任務已啟動，請查看日誌");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 獲取排程狀態（預留）
     * GET /scheduler/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSchedulerStatus() {
        Map<String, Object> result = new HashMap<>();

        result.put("schedulerEnabled", true);
        result.put("weeklyUpdateCron", "0 0 2 * * SUN");
        result.put("description", "每週日凌晨 2:00 自動更新餐廳資料");

        return ResponseEntity.ok(result);
    }
}
