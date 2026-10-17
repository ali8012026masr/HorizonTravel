package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideBooking {
    @SerializedName("id")
    private Long id;

    @SerializedName("touristId")
    private Long touristId;

    @SerializedName("tourGuideId")
    private Long tourGuideId;

    @SerializedName("guideAvailabilityId")
    private Long guideAvailabilityId;

    @SerializedName("scheduleDate")
    private String scheduleDate;

    @SerializedName("agreedPrice")
    private double agreedPrice;

    @SerializedName(value = "isNegotiated", alternate = {"negotiated"})
    private Boolean isNegotiated;

    @SerializedName("status")
    private String status;

    @SerializedName(value = "paymentReceived", alternate = {"isPaymentReceived"})
    private Boolean paymentReceived;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    public GuideBooking() {}

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

    public Long getTourGuideId() {
        return tourGuideId;
    }

    public void setTourGuideId(Long tourGuideId) {
        this.tourGuideId = tourGuideId;
    }

    public Long getGuideAvailabilityId() {
        return guideAvailabilityId;
    }

    public void setGuideAvailabilityId(Long guideAvailabilityId) {
        this.guideAvailabilityId = guideAvailabilityId;
    }

    public String getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public double getAgreedPrice() {
        return agreedPrice;
    }

    public void setAgreedPrice(double agreedPrice) {
        this.agreedPrice = agreedPrice;
    }

    public Boolean getNegotiated() {
        return isNegotiated;
    }

    public void setNegotiated(Boolean negotiated) {
        isNegotiated = negotiated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPaymentReceived() {
        return paymentReceived;
    }

    public void setPaymentReceived(Boolean paymentReceived) {
        this.paymentReceived = paymentReceived;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
