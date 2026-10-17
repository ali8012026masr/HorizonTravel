package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class VerifyRequest {
    @SerializedName(value = "approved", alternate = {"isApproved"})
    private boolean approved;

    @SerializedName("rejectionReason")
    private String rejectionReason;

    public VerifyRequest() {}

    public VerifyRequest(boolean approved, String rejectionReason) {
        this.approved = approved;
        this.rejectionReason = rejectionReason;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
