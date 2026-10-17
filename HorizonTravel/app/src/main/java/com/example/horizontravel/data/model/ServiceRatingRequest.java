package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class ServiceRatingRequest {
    @SerializedName("bookingId")
    private Long bookingId;

    @SerializedName("touristId")
    private Long touristId;

    @SerializedName("providerId")
    private Long providerId;

    @SerializedName("score")
    private int score;

    @SerializedName("comment")
    private String comment;

    public ServiceRatingRequest() {}

    public ServiceRatingRequest(Long bookingId, Long touristId, Long providerId, int score, String comment) {
        this.bookingId = bookingId;
        this.touristId = touristId;
        this.providerId = providerId;
        this.score = score;
        this.comment = comment;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getTouristId() {
        return touristId;
    }

    public void setTouristId(Long touristId) {
        this.touristId = touristId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
