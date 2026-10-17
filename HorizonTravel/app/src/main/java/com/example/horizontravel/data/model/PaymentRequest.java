package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentRequest {
    @SerializedName("bookingId")
    private Long bookingId;

    @SerializedName("guideBookingId")
    private Long guideBookingId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("method")
    private String method;

    public PaymentRequest() {}

    public PaymentRequest(Long bookingId, Long guideBookingId, double amount, String method) {
        this.bookingId = bookingId;
        this.guideBookingId = guideBookingId;
        this.amount = amount;
        this.method = method;
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
}
