package com.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * API 測試控制器
 * 如果爆掉，測試Google Places API有沒有問題
 */
@RestController
@RequestMapping("/api-test")
public class ApiTestController {

    @Value("${google.places.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 直接測試 Google Places API（完全複製 curl 成功的 URL）
     */
    @GetMapping("/test-direct")
    public ResponseEntity<Map<String, Object>> testDirect() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 完全複製 curl 成功的 URL
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=台北市信義區%20餐廳&language=zh-TW&key=" + apiKey;

            result.put("url", url);
            result.put("apiKeyPrefix", apiKey.substring(0, 10) + "...");

            String response = restTemplate.getForObject(url, String.class);
            result.put("response", response);
            result.put("responseLength", response != null ? response.length() : 0);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorClass", e.getClass().getName());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 測試英文查詢
     */
    @GetMapping("/test-english")
    public ResponseEntity<Map<String, Object>> testEnglish() {
        Map<String, Object> result = new HashMap<>();

        try {
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=restaurants+in+Taipei&language=zh-TW&key=" + apiKey;

            result.put("url", url);

            String response = restTemplate.getForObject(url, String.class);
            result.put("response", response);
            result.put("responseLength", response != null ? response.length() : 0);

        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
