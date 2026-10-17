package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PendingRegistrationsResponse {
    @SerializedName("providers")
    private List<Provider> providers;

    @SerializedName("guides")
    private List<Guide> guides;

    public PendingRegistrationsResponse() {}

    public List<Provider> getProviders() {
        return providers;
    }

    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }

    public List<Guide> getGuides() {
        return guides;
    }

    public void setGuides(List<Guide> guides) {
        this.guides = guides;
    }
}
