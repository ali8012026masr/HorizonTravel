package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class CommissionSummary {
    @SerializedName("totalCommission")
    private double totalCommission;

    @SerializedName("pendingCommission")
    private double pendingCommission;

    @SerializedName("settledCommission")
    private double settledCommission;

    public CommissionSummary() {}

    public double getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(double totalCommission) {
        this.totalCommission = totalCommission;
    }

    public double getPendingCommission() {
        return pendingCommission;
    }

    public void setPendingCommission(double pendingCommission) {
        this.pendingCommission = pendingCommission;
    }

    public double getSettledCommission() {
        return settledCommission;
    }

    public void setSettledCommission(double settledCommission) {
        this.settledCommission = settledCommission;
    }
}
