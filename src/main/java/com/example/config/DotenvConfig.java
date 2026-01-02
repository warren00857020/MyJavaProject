package com.example.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 載入 .env 檔案的配置
 * 這個類會在 Spring Boot 啟動時自動載入 .env 檔案中的環境變數
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // 載入 .env 檔案（如果存在）
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing() // 如果檔案不存在則忽略（適用於 Railway 等雲端環境）
                    .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> envMap = new HashMap<>();

            // 將所有 .env 中的變數加入 Spring Environment
            dotenv.entries().forEach(entry -> {
                envMap.put(entry.getKey(), entry.getValue());
                System.out.println("✅ 載入環境變數: " + entry.getKey() + " = " +
                    (entry.getKey().contains("KEY") || entry.getKey().contains("PASSWORD")
                        ? "***隱藏***"
                        : entry.getValue()));
            });

            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", envMap));

        } catch (Exception e) {
            System.err.println("⚠️ 載入 .env 檔案失敗（可能是在雲端環境）: " + e.getMessage());
        }
    }
}
