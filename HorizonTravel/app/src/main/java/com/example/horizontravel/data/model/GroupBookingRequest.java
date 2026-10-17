package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupBookingRequest {
    @SerializedName("bookingId")
    private Long bookingId;

    public GroupBookingRequest() {}

    public GroupBookingRequest(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
}
