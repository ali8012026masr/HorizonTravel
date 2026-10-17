package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class RevenueSummary {
    @SerializedName("totalRevenue")
    private double totalRevenue;

    @SerializedName("totalCommission")
    private double totalCommission;

    @SerializedName("paymentCount")
    private int paymentCount;

    public RevenueSummary() {}

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(double totalCommission) {
        this.totalCommission = totalCommission;
    }

    public int getPaymentCount() {
        return paymentCount;
    }

    public void setPaymentCount(int paymentCount) {
        this.paymentCount = paymentCount;
    }
}
