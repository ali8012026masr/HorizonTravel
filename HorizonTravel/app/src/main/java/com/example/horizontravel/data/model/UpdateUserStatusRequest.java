package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateUserStatusRequest {
    @SerializedName("status")
    private String status;

    public UpdateUserStatusRequest() {}

    public UpdateUserStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
