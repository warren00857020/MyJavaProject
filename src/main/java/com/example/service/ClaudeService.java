package com.example.service;

import com.example.dto.ChatRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Claude API 服務
 */
@Service
public class ClaudeService {

    @Value("${claude.api.key:}")
    private String apiKey;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 發送聊天請求到 Claude API
     */
    public String chat(List<ChatRequest.Message> messages, boolean enableTools) throws Exception {

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Claude API Key 未設定，請在環境變數中設定 CLAUDE_API_KEY");
        }

        // 建立請求
        ChatRequest request = new ChatRequest();
        request.setMessages(messages);

        // 如果啟用工具，加入台灣旅遊相關的工具
        if (enableTools) {
            request.setTools(getTaiwanTravelTools());
        }

        // 設定 Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);

        // 發送請求
        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            CLAUDE_API_URL,
            HttpMethod.POST,
            entity,
            String.class
        );

        // 解析回應
        JsonNode responseJson = objectMapper.readTree(response.getBody());

        // 處理 tool_use（如果有）
        if (responseJson.has("content")) {
            JsonNode content = responseJson.get("content");
            if (content.isArray() && content.size() > 0) {
                JsonNode firstContent = content.get(0);

                // 如果是 tool_use，執行工具並繼續對話
                if (firstContent.has("type") && "tool_use".equals(firstContent.get("type").asText())) {
                    return handleToolUse(messages, responseJson);
                }

                // 一般文字回應
                if (firstContent.has("text")) {
                    return firstContent.get("text").asText();
                }
            }
        }

        return response.getBody();
    }

    /**
     * 處理工具呼叫
     */
    private String handleToolUse(List<ChatRequest.Message> originalMessages, JsonNode claudeResponse) throws Exception {
        // 這裡可以實作工具呼叫邏輯
        // 暫時先返回原始回應
        return claudeResponse.toString();
    }

    /**
     * 定義台灣旅遊相關的工具
     */
    private List<ChatRequest.Tool> getTaiwanTravelTools() {
        List<ChatRequest.Tool> tools = new ArrayList<>();

        // 景點搜尋工具
        ChatRequest.Tool searchSights = new ChatRequest.Tool();
        searchSights.setName("search_sights");
        searchSights.setDescription("搜尋台灣的景點。支援按城市、區域、關鍵字搜尋。");

        Map<String, Object> sightsSchema = new HashMap<>();
        sightsSchema.put("type", "object");
        sightsSchema.put("properties", Map.of(
            "city", Map.of("type", "string", "description", "城市名稱，例如：台北市"),
            "zone", Map.of("type", "string", "description", "區域名稱，例如：信義區"),
            "keyword", Map.of("type", "string", "description", "搜尋關鍵字"),
            "limit", Map.of("type", "number", "description", "返回筆數，預設20")
        ));
        searchSights.setInputSchema(sightsSchema);
        tools.add(searchSights);

        // 美食搜尋工具
        ChatRequest.Tool searchFoods = new ChatRequest.Tool();
        searchFoods.setName("search_foods");
        searchFoods.setDescription("搜尋台灣的美食和餐廳。");

        Map<String, Object> foodsSchema = new HashMap<>();
        foodsSchema.put("type", "object");
        foodsSchema.put("properties", Map.of(
            "keyword", Map.of("type", "string", "description", "搜尋關鍵字，例如：拉麵、火鍋"),
            "zone", Map.of("type", "string", "description", "區域名稱"),
            "limit", Map.of("type", "number", "description", "返回筆數，預設20")
        ));
        searchFoods.setInputSchema(foodsSchema);
        tools.add(searchFoods);

        return tools;
    }
}
