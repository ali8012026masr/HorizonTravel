package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AiChatRequest {
    @SerializedName("userMessage")
    private String userMessage;

    @SerializedName("history")
    private List<AiChatMessage> history;

    public AiChatRequest() {}

    public AiChatRequest(String userMessage, List<AiChatMessage> history) {
        this.userMessage = userMessage;
        this.history = history;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public List<AiChatMessage> getHistory() {
        return history;
    }

    public void setHistory(List<AiChatMessage> history) {
        this.history = history;
    }
}
