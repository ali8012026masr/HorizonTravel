package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class ServiceRating {
    @SerializedName("id")
    private Long id;

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

    @SerializedName("ratedDate")
    private String ratedDate;

    public ServiceRating() {}

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

    public String getRatedDate() {
        return ratedDate;
    }

    public void setRatedDate(String ratedDate) {
        this.ratedDate = ratedDate;
    }
}
