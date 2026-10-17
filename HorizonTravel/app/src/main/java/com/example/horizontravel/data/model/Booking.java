package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName("id")
    private Long id;

    @SerializedName("touristId")
    private Long touristId;

    @SerializedName("serviceSlotId")
    private Long serviceSlotId;

    @SerializedName("bookingDate")
    private String bookingDate;

    @SerializedName("status")
    private String status;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    public Booking() {}

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

    public Long getServiceSlotId() {
        return serviceSlotId;
    }

    public void setServiceSlotId(Long serviceSlotId) {
        this.serviceSlotId = serviceSlotId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
