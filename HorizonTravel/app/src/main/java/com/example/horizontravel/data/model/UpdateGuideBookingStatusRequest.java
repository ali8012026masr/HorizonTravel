package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateGuideBookingStatusRequest {
    @SerializedName("status")
    private String status;

    public UpdateGuideBookingStatusRequest() {}

    public UpdateGuideBookingStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
