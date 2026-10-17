package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideBookingRequest {
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

    public GuideBookingRequest() {}

    public GuideBookingRequest(Long touristId, Long tourGuideId, Long guideAvailabilityId,
                               String scheduleDate, double agreedPrice, Boolean isNegotiated) {
        this.touristId = touristId;
        this.tourGuideId = tourGuideId;
        this.guideAvailabilityId = guideAvailabilityId;
        this.scheduleDate = scheduleDate;
        this.agreedPrice = agreedPrice;
        this.isNegotiated = isNegotiated;
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
}
