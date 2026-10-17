package com.example.horizontravel.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Group {
    @SerializedName("id")
    private Long id;

    @SerializedName("groupName")
    private String groupName;

    @SerializedName("createdByTouristId")
    private Long createdByTouristId;

    @SerializedName("joinCode")
    private String joinCode;

    @SerializedName("bookingId")
    private Long bookingId;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("members")
    private List<GroupMember> members;

    public Group() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<GroupMember> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMember> members) {
        this.members = members;
    }
}
