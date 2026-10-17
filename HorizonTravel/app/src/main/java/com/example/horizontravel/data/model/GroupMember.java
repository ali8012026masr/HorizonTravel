package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupMember {
    @SerializedName("id")
    private Long id;

    @SerializedName("touristId")
    private Long touristId;

    @SerializedName("touristName")
    private String touristName;

    @SerializedName("status")
    private String status;

    @SerializedName("joinedAt")
    private String joinedAt;

    @SerializedName("amountOwed")
    private double amountOwed;

    @SerializedName(value = "paid", alternate = {"isPaid"})
    private Boolean paid;

    public GroupMember() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTouristId() {
        return touristId;
    }

    public void setTouristId(Long touristId) {
        this.touristId = touristId;
    }

    public String getTouristName() {
        return touristName;
    }

    public void setTouristName(String touristName) {
        this.touristName = touristName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }

    public double getAmountOwed() {
        return amountOwed;
    }

    public void setAmountOwed(double amountOwed) {
        this.amountOwed = amountOwed;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }
}
