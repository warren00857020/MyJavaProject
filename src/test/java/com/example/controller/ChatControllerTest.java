package com.example.controller;

import com.example.dto.SimpleChatRequest;
import com.example.service.ClaudeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ChatController 整合測試
 * 測試 REST API 端點功能
 */
@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaudeService claudeService;

    /**
     * 測試：健康檢查端點
     */
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/chat/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.service").value("Chat API"));
    }

    /**
     * 測試：取得模型列表
     */
    @Test
    void testGetModels() throws Exception {
        mockMvc.perform(get("/api/chat/models"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.models").isArray())
            .andExpect(jsonPath("$.models[0].id").exists())
            .andExpect(jsonPath("$.models[0].name").exists());
    }

    /**
     * 測試：成功的聊天請求
     */
    @Test
    void testSuccessfulChatRequest() throws Exception {
        // Mock ClaudeService 回應
        when(claudeService.chat(anyList(), anyBoolean()))
            .thenReturn("根據搜尋結果，台北有以下景點：台北101、故宮博物院等。");

        SimpleChatRequest request = new SimpleChatRequest();
        request.setMessage("推薦台北景點");
        request.setHistory(new ArrayList<>());

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").isString())
            .andExpect(jsonPath("$.message").value(containsString("台北")));
    }

    /**
     * 測試：空訊息應該返回錯誤
     */
    @Test
    void testEmptyMessageReturnsError() throws Exception {
        SimpleChatRequest request = new SimpleChatRequest();
        request.setMessage("");
        request.setHistory(new ArrayList<>());

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("訊息不能為空"));
    }

    /**
     * 測試：null 訊息應該返回錯誤
     */
    @Test
    void testNullMessageReturnsError() throws Exception {
        SimpleChatRequest request = new SimpleChatRequest();
        request.setMessage(null);
        request.setHistory(new ArrayList<>());

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    /**
     * 測試：包含對話歷史的請求
     */
    @Test
    void testChatWithHistory() throws Exception {
        when(claudeService.chat(anyList(), anyBoolean()))
            .thenReturn("好的，除了台北101，還有故宮博物院、龍山寺等景點。");

        SimpleChatRequest request = new SimpleChatRequest();
        request.setMessage("還有其他推薦嗎？");

        SimpleChatRequest.ChatHistory history1 = new SimpleChatRequest.ChatHistory();
        history1.setRole("user");
        history1.setContent("推薦台北景點");

        SimpleChatRequest.ChatHistory history2 = new SimpleChatRequest.ChatHistory();
        history2.setRole("assistant");
        history2.setContent("推薦您去台北101");

        ArrayList<SimpleChatRequest.ChatHistory> history = new ArrayList<>();
        history.add(history1);
        history.add(history2);
        request.setHistory(history);

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").isString());
    }

    /**
     * 測試：ClaudeService 拋出異常時的錯誤處理
     */
    @Test
    void testChatServiceException() throws Exception {
        when(claudeService.chat(anyList(), anyBoolean()))
            .thenThrow(new RuntimeException("Claude API 連線失敗"));

        SimpleChatRequest request = new SimpleChatRequest();
        request.setMessage("測試錯誤處理");
        request.setHistory(new ArrayList<>());

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists());
    }

    /**
     * 測試：IllegalStateException（API Key 未設定）的錯誤處理
     */
    @Test
    void testApiKeyNotConfigured() throws Exception {
        when(claudeService.chat(anyList(), anyBoolean()))
            .thenThrow(new IllegalStateException("Claude API Key 未設定"));

        SimpleChatRequest request = new SimpleChatRequest();
        request.setMessage("測試 API Key 錯誤");
        request.setHistory(new ArrayList<>());

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Claude API Key 未設定"))
            .andExpect(jsonPath("$.hint").exists());
    }

    /**
     * 測試：CORS 標頭應該允許跨域請求
     */
    @Test
    void testCorsHeaders() throws Exception {
        when(claudeService.chat(anyList(), anyBoolean()))
            .thenReturn("測試回應");

        SimpleChatRequest request = new SimpleChatRequest();
        request.setMessage("測試");
        request.setHistory(new ArrayList<>());

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Origin", "http://localhost:3000"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
