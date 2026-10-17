package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupMessage {
    @SerializedName("id")
    private Long id;

    @SerializedName("senderId")
    private Long senderId;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("message")
    private String message;

    @SerializedName("sentAt")
    private String sentAt;

    public GroupMessage() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }
}
