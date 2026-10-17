package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class Commission {
    @SerializedName("id")
    private Long id;

    @SerializedName("bookingId")
    private Long bookingId;

    @SerializedName("providerName")
    private String providerName;

    @SerializedName("serviceCategory")
    private String serviceCategory;

    @SerializedName("percentage")
    private double percentage;

    @SerializedName("amount")
    private double amount;

    @SerializedName("calculatedDate")
    private String calculatedDate;

    @SerializedName("settlementStatus")
    private String settlementStatus;

    public Commission() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCalculatedDate() {
        return calculatedDate;
    }

    public void setCalculatedDate(String calculatedDate) {
        this.calculatedDate = calculatedDate;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }
}
