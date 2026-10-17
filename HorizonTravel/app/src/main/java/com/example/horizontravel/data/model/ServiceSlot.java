package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class ServiceSlot {
    @SerializedName("id")
    private Long id;

    @SerializedName("providerId")
    private Long providerId;

    @SerializedName("providerBusinessName")
    private String providerBusinessName;

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

    @SerializedName("availableSeats")
    private Integer availableSeats;

    @SerializedName("price")
    private double price;

    @SerializedName("status")
    private String status;

    @SerializedName("providerRatingAvg")
    private Double providerRatingAvg;

    @SerializedName("providerRatingCount")
    private Integer providerRatingCount;

    public ServiceSlot() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getProviderBusinessName() {
        return providerBusinessName;
    }

    public void setProviderBusinessName(String providerBusinessName) {
        this.providerBusinessName = providerBusinessName;
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

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getProviderRatingAvg() {
        return providerRatingAvg;
    }

    public void setProviderRatingAvg(Double providerRatingAvg) {
        this.providerRatingAvg = providerRatingAvg;
    }

    public Integer getProviderRatingCount() {
        return providerRatingCount;
    }

    public void setProviderRatingCount(Integer providerRatingCount) {
        this.providerRatingCount = providerRatingCount;
    }
}
