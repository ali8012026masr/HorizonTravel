package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupCreateRequest {
    @SerializedName("groupName")
    private String groupName;

    @SerializedName("createdByTouristId")
    private Long createdByTouristId;

    public GroupCreateRequest() {}

    public GroupCreateRequest(String groupName, Long createdByTouristId) {
        this.groupName = groupName;
        this.createdByTouristId = createdByTouristId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getCreatedByTouristId() {
        return createdByTouristId;
    }

    public void setCreatedByTouristId(Long createdByTouristId) {
        this.createdByTouristId = createdByTouristId;
    }
}
