package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class BookingRequest {
    @SerializedName("touristId")
    private Long touristId;

    @SerializedName("serviceSlotId")
    private Long serviceSlotId;

    public BookingRequest() {}

    public BookingRequest(Long touristId, Long serviceSlotId) {
        this.touristId = touristId;
        this.serviceSlotId = serviceSlotId;
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
}
