package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideRatingRequest {
    @SerializedName("guideBookingId")
    private Long guideBookingId;

    @SerializedName("score")
    private int score;

    @SerializedName("comment")
    private String comment;

    public GuideRatingRequest() {}

    public GuideRatingRequest(Long guideBookingId, int score, String comment) {
        this.guideBookingId = guideBookingId;
        this.score = score;
        this.comment = comment;
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
}
