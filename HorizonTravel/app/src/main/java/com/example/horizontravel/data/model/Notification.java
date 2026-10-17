package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    private Long id;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private String type;

    @SerializedName(value = "read", alternate = {"isRead"})
    private Boolean read;

    @SerializedName("createdAt")
    private String createdAt;

    public Notification() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
