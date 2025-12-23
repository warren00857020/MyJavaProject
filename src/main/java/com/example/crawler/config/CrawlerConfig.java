package com.example.crawler.config;

import org.springframework.context.annotation.Configuration;

import java.util.Random;

/**
 * 爬蟲配置類
 * 提供 User-Agent 輪換、延遲時間等配置
 */
@Configuration
public class CrawlerConfig {

    // 基礎 URL
    public static final String BASE_URL = "https://www.travelking.com.tw";

    // 重試限制
    public static final int RETRY_LIMIT = 3;

    // 延遲時間 (毫秒)
    public static final long MIN_DELAY_MS = 1000;  // 1 秒
    public static final long MAX_DELAY_MS = 3000;  // 3 秒

    // 批次處理大小
    public static final int BATCH_SIZE = 100;

    // User-Agent 列表（模擬不同瀏覽器）
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    };

    private static final Random random = new Random();

    /**
     * 隨機獲取 User-Agent
     */
    public static String getRandomUserAgent() {
        return USER_AGENTS[random.nextInt(USER_AGENTS.length)];
    }

    /**
     * 獲取隨機延遲時間（毫秒）
     */
    public static long getRandomDelay() {
        return MIN_DELAY_MS + random.nextInt((int)(MAX_DELAY_MS - MIN_DELAY_MS + 1));
    }
}
