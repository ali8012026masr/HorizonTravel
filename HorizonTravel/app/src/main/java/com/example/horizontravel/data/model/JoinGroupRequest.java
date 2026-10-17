package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class JoinGroupRequest {
    @SerializedName("joinCode")
    private String joinCode;

    @SerializedName("touristId")
    private Long touristId;

    public JoinGroupRequest() {}

    public JoinGroupRequest(String joinCode, Long touristId) {
        this.joinCode = joinCode;
        this.touristId = touristId;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public Long getTouristId() {
        return touristId;
    }

    public void setTouristId(Long touristId) {
        this.touristId = touristId;
    }
}
