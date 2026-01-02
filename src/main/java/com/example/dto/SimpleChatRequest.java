package com.example.dto;

import java.util.List;

/**
 * 前端發送的簡化聊天請求
 */
public class SimpleChatRequest {

    private String message;
    private List<ChatHistory> history;

    public static class ChatHistory {
        private String role;
        private String content;

        public ChatHistory() {}

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public SimpleChatRequest() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<ChatHistory> getHistory() { return history; }
    public void setHistory(List<ChatHistory> history) { this.history = history; }
}
