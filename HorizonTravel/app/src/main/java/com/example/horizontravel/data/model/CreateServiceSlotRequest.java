package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class CreateServiceSlotRequest {
    @SerializedName("providerId")
    private Long providerId;

    @SerializedName("category")
    private String category;

    @SerializedName("origin")
    private String origin;

    @SerializedName("destination")
    private String destination;

    @SerializedName("locationName")
    private String locationName;

    @SerializedName("startDateTime")
    private String startDateTime;

    @SerializedName("endDateTime")
    private String endDateTime;

    @SerializedName("capacity")
    private Integer capacity;

    @SerializedName("price")
    private double price;

    public CreateServiceSlotRequest() {}

    public CreateServiceSlotRequest(Long providerId, String category, String origin, String destination,
                                    String locationName, String startDateTime, String endDateTime,
                                    Integer capacity, double price) {
        this.providerId = providerId;
        this.category = category;
        this.origin = origin;
        this.destination = destination;
        this.locationName = locationName;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.capacity = capacity;
        this.price = price;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
