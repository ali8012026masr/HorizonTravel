package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideRating {
    @SerializedName("id")
    private Long id;

    @SerializedName("guideBookingId")
    private Long guideBookingId;

    @SerializedName("score")
    private int score;

    @SerializedName("comment")
    private String comment;

    @SerializedName("ratedDate")
    private String ratedDate;

    public GuideRating() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGuideBookingId() {
        return guideBookingId;
    }

    public void setGuideBookingId(Long guideBookingId) {
        this.guideBookingId = guideBookingId;
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
