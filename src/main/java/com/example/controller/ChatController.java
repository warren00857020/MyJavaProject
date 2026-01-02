package com.example.controller;

import com.example.dto.ChatRequest;
import com.example.dto.SimpleChatRequest;
import com.example.service.ClaudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Chat API Controller
 * 提供與 Claude AI 的聊天介面
 */
@RestController
@RequestMapping(value = "/api/chat", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*") // 允許前端跨域請求
public class ChatController {

    @Autowired
    private ClaudeService claudeService;

    /**
     * 簡化的聊天 API（給前端使用）
     * POST /api/chat
     *
     * Request Body:
     * {
     *   "message": "推薦台北的景點",
     *   "history": [
     *     {"role": "user", "content": "你好"},
     *     {"role": "assistant", "content": "你好！"}
     *   ]
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody SimpleChatRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 驗證請求
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "訊息不能為空");
                return ResponseEntity.badRequest().body(response);
            }

            // 建立訊息列表（包含歷史訊息）
            List<ChatRequest.Message> messages = new ArrayList<>();

            // 加入系統提示詞
            messages.add(new ChatRequest.Message(
                "user",
                "你是台灣旅遊智能助手，專門幫助使用者規劃台灣旅遊行程、推薦景點和美食。" +
                "你可以使用 search_sights 和 search_foods 工具來查詢即時資料。" +
                "請用繁體中文回答，並提供實用的建議。"
            ));
            messages.add(new ChatRequest.Message("assistant", "了解！我會幫您推薦台灣的景點和美食。"));

            // 加入歷史對話
            if (request.getHistory() != null && !request.getHistory().isEmpty()) {
                for (SimpleChatRequest.ChatHistory history : request.getHistory()) {
                    messages.add(new ChatRequest.Message(history.getRole(), history.getContent()));
                }
            }

            // 加入當前訊息
            messages.add(new ChatRequest.Message("user", request.getMessage()));

            // 呼叫 Claude API（暫時不啟用工具，先測試基本功能）
            String claudeResponse = claudeService.chat(messages, false);

            response.put("success", true);
            response.put("message", claudeResponse);
            response.put("model", "claude-3-5-sonnet-20241022");

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            // API Key 未設定
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("hint", "請在 Railway 環境變數中設定 CLAUDE_API_KEY");
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "聊天失敗: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());

            // 詳細的錯誤日誌
            System.err.println("❌ Chat API 錯誤:");
            System.err.println("   訊息: " + request.getMessage());
            System.err.println("   錯誤: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 健康檢查
     * GET /api/chat/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Chat API");
        response.put("timestamp", new Date());
        return ResponseEntity.ok(response);
    }

    /**
     * 取得可用的模型列表（模擬）
     * GET /api/chat/models
     */
    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> getModels() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> models = new ArrayList<>();

        Map<String, String> sonnet = new HashMap<>();
        sonnet.put("id", "claude-3-5-sonnet-20241022");
        sonnet.put("name", "Claude 3.5 Sonnet");
        sonnet.put("description", "最新最強的 Claude 模型");
        models.add(sonnet);

        response.put("models", models);
        return ResponseEntity.ok(response);
    }
}
