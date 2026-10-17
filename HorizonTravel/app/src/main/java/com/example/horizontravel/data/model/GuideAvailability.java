package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideAvailability {
    @SerializedName("id")
    private Long id;

    @SerializedName("tourGuideId")
    private Long tourGuideId;

    @SerializedName("tourGuideName")
    private String tourGuideName;

    @SerializedName("date")
    private String date;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("location")
    private String location;

    @SerializedName("notes")
    private String notes;

    @SerializedName("status")
    private String status;

    @SerializedName("tourGuideRatingAvg")
    private Double tourGuideRatingAvg;

    @SerializedName("tourGuideRatingCount")
    private Integer tourGuideRatingCount;

    public GuideAvailability() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTourGuideId() {
        return tourGuideId;
    }

    public void setTourGuideId(Long tourGuideId) {
        this.tourGuideId = tourGuideId;
    }

    public String getTourGuideName() {
        return tourGuideName;
    }

    public void setTourGuideName(String tourGuideName) {
        this.tourGuideName = tourGuideName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTourGuideRatingAvg() {
        return tourGuideRatingAvg;
    }

    public void setTourGuideRatingAvg(Double tourGuideRatingAvg) {
        this.tourGuideRatingAvg = tourGuideRatingAvg;
    }

    public Integer getTourGuideRatingCount() {
        return tourGuideRatingCount;
    }

    public void setTourGuideRatingCount(Integer tourGuideRatingCount) {
        this.tourGuideRatingCount = tourGuideRatingCount;
    }
}
