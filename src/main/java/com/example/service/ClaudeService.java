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

    @Value("${claude.mock.enabled:false}")
    private boolean mockEnabled;

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 發送聊天請求到 Claude API
     */
    public String chat(List<ChatRequest.Message> messages, boolean enableTools) throws Exception {

        // 如果啟用 Mock 模式，返回模擬回應
        if (mockEnabled) {
            return getMockResponse(messages, enableTools);
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Claude API Key 未設定，請在環境變數中設定 CLAUDE_API_KEY");
        }

        System.out.println("🤖 呼叫 Claude API...");
        System.out.println("   訊息數量: " + messages.size());
        System.out.println("   啟用工具: " + enableTools);

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

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                CLAUDE_API_URL,
                HttpMethod.POST,
                entity,
                String.class
            );
            System.out.println("✅ Claude API 回應成功");
        } catch (Exception e) {
            System.err.println("❌ Claude API 呼叫失敗: " + e.getMessage());
            throw new Exception("Claude API 呼叫失敗: " + e.getMessage(), e);
        }

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

    /**
     * 模擬 Claude API 回應（用於 Demo 或額度不足時）
     */
    private String getMockResponse(List<ChatRequest.Message> messages, boolean enableTools) {
        System.out.println("🎭 使用 Mock 模式（模擬 Claude 回應）");

        // 取得最後一個使用者訊息
        String lastUserMessage = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).getRole())) {
                lastUserMessage = messages.get(i).getContent().toLowerCase();
                break;
            }
        }

        // 根據關鍵字返回不同的模擬回應
        if (lastUserMessage.contains("tool") || lastUserMessage.contains("功能")) {
            return "我是台灣旅遊智能助手！目前我可以幫你：\n\n" +
                   "🏞️ **景點推薦** - 搜尋台灣各地的熱門景點\n" +
                   "🍜 **美食探索** - 尋找在地特色餐廳\n" +
                   "📅 **行程規劃** - 幫你安排一日遊或多日遊\n" +
                   "🎯 **旅遊建議** - 提供交通、住宿等實用資訊\n\n" +
                   "試試問我：「推薦台北的景點」或「台中有什麼好吃的？」\n\n" +
                   "💡 *目前使用模擬模式 (Mock Mode)，實際部署後會使用真實的 Claude AI*";
        } else if (lastUserMessage.contains("台北") || lastUserMessage.contains("景點")) {
            return "台北有很多值得一訪的景點！以下是我的推薦：\n\n" +
                   "🏛️ **故宮博物院** - 世界級的中華文物收藏\n" +
                   "🌃 **台北101** - 地標建築，觀景台視野絕佳\n" +
                   "🎨 **華山文創園區** - 文青必訪，結合藝術與美食\n" +
                   "🌲 **象山步道** - 欣賞台北夜景的最佳地點\n" +
                   "🏮 **龍山寺** - 歷史悠久的宗教建築\n\n" +
                   "需要我幫你規劃詳細的一日遊行程嗎？\n\n" +
                   "💡 *模擬回應 - 實際使用時會連接真實資料庫*";
        } else if (lastUserMessage.contains("美食") || lastUserMessage.contains("好吃") || lastUserMessage.contains("餐廳")) {
            return "台灣美食真的超讚！以下是我的推薦：\n\n" +
                   "🥟 **鼎泰豐** - 米其林推薦的小籠包\n" +
                   "🍜 **阿宗麵線** - 台北必吃的傳統小吃\n" +
                   "🥘 **欣葉台菜** - 道地的台灣料理\n" +
                   "🍰 **春水堂** - 珍珠奶茶的創始店\n" +
                   "🦐 **饒河街夜市** - 各種夜市美食一次滿足\n\n" +
                   "想知道更多特定區域的美食推薦嗎？\n\n" +
                   "💡 *模擬回應 - 實際版本會使用 Google Places API*";
        } else if (lastUserMessage.contains("行程") || lastUserMessage.contains("規劃")) {
            return "讓我幫你規劃一個台北一日遊行程：\n\n" +
                   "**上午** (9:00-12:00)\n" +
                   "🏛️ 故宮博物院 - 欣賞國寶級文物\n\n" +
                   "**中午** (12:00-14:00)\n" +
                   "🍜 士林夜市 - 品嚐道地小吃\n\n" +
                   "**下午** (14:00-17:00)\n" +
                   "🌃 台北101 - 登上觀景台\n\n" +
                   "**傍晚** (17:00-19:00)\n" +
                   "🌲 象山步道 - 欣賞夕陽和夜景\n\n" +
                   "**晚上** (19:00-21:00)\n" +
                   "🏮 西門町 - 逛街購物、享用晚餐\n\n" +
                   "需要我調整行程或提供交通建議嗎？\n\n" +
                   "💡 *模擬回應 - 實際版本會根據即時資料客製化*";
        } else {
            return "你好！我是台灣旅遊智能助手 🗺️\n\n" +
                   "我可以幫你：\n" +
                   "• 推薦景點和美食\n" +
                   "• 規劃旅遊行程\n" +
                   "• 提供交通和住宿建議\n\n" +
                   "試試問我：\n" +
                   "「推薦台北的景點」\n" +
                   "「台中有什麼好吃的？」\n" +
                   "「幫我規劃台南一日遊」\n\n" +
                   "💡 *目前使用模擬模式，充值 Claude API 額度後即可使用真實 AI*";
        }
    }
}
