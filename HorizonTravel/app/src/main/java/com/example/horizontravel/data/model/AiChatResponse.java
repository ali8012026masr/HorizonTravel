package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class AiChatResponse {
    @SerializedName("replyText")
    private String replyText;

    @SerializedName("actionType")
    private String actionType;

    @SerializedName("actionPayloadJson")
    private String actionPayloadJson;

    public AiChatResponse() {}

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionPayloadJson() {
        return actionPayloadJson;
    }

    public void setActionPayloadJson(String actionPayloadJson) {
        this.actionPayloadJson = actionPayloadJson;
    }
}
