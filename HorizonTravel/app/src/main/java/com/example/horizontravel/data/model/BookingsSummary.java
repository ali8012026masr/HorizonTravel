package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class BookingsSummary {
    @SerializedName("totalBookings")
    private int totalBookings;

    @SerializedName("pendingCount")
    private int pendingCount;

    @SerializedName("confirmedCount")
    private int confirmedCount;

    @SerializedName("cancelledCount")
    private int cancelledCount;

    @SerializedName("completedCount")
    private int completedCount;

    public BookingsSummary() {}

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(int pendingCount) {
        this.pendingCount = pendingCount;
    }

    public int getConfirmedCount() {
        return confirmedCount;
    }

    public void setConfirmedCount(int confirmedCount) {
        this.confirmedCount = confirmedCount;
    }

    public int getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(int cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }
}
