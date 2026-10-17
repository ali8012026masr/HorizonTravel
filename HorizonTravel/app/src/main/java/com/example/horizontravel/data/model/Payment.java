package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class Payment {
    @SerializedName("id")
    private Long id;

    @SerializedName("bookingId")
    private Long bookingId;

    @SerializedName("guideBookingId")
    private Long guideBookingId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("method")
    private String method;

    @SerializedName("status")
    private String status;

    @SerializedName("transactionDate")
    private String transactionDate;

    public Payment() {}

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

    public Long getGuideBookingId() {
        return guideBookingId;
    }

    public void setGuideBookingId(Long guideBookingId) {
        this.guideBookingId = guideBookingId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }
}
