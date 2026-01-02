package com.example.dto;

import java.util.List;

/**
 * Claude API 聊天請求 DTO
 */
public class ChatRequest {

    private String model = "claude-3-5-sonnet-20241022";
    private Integer maxTokens = 4096;
    private List<Message> messages;
    private List<Tool> tools;

    public static class Message {
        private String role;
        private String content;

        public Message() {}

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class Tool {
        private String name;
        private String description;
        private Object inputSchema;

        public Tool() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Object getInputSchema() { return inputSchema; }
        public void setInputSchema(Object inputSchema) { this.inputSchema = inputSchema; }
    }

    public ChatRequest() {}

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    public List<Tool> getTools() { return tools; }
    public void setTools(List<Tool> tools) { this.tools = tools; }
}
