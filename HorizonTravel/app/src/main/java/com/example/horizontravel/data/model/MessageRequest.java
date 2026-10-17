package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class MessageRequest {
    @SerializedName("touristId")
    private Long touristId;

    @SerializedName("message")
    private String message;

    public MessageRequest() {}

    public MessageRequest(Long touristId, String message) {
        this.touristId = touristId;
        this.message = message;
    }

    public Long getTouristId() {
        return touristId;
    }

    public void setTouristId(Long touristId) {
        this.touristId = touristId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
