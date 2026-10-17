package com.example.horizontravel.data.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    @SerializedName("roleDetails")
    private JsonElement roleDetails;

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("refreshToken")
    private String refreshToken;

    @SerializedName("expiresInSeconds")
    private Long expiresInSeconds;

    public LoginResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public JsonObject getRoleDetails() {
        if (roleDetails != null && roleDetails.isJsonObject()) {
            return roleDetails.getAsJsonObject();
        }
        return null;
    }

    public void setRoleDetails(JsonElement roleDetails) {
        this.roleDetails = roleDetails;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getStatus() {
        if (roleDetails != null && roleDetails.isJsonObject()) {
            JsonObject obj = roleDetails.getAsJsonObject();
            if (obj.has("status") && !obj.get("status").isJsonNull()) {
                return obj.get("status").getAsString();
            }
        }
        return null;
    }

    public String getVerificationStatus() {
        if (roleDetails != null && roleDetails.isJsonObject()) {
            JsonObject obj = roleDetails.getAsJsonObject();
            if (obj.has("verificationStatus") && !obj.get("verificationStatus").isJsonNull()) {
                return obj.get("verificationStatus").getAsString();
            }
        }
        return null;
    }

    public String getRejectionReason() {
        if (roleDetails != null && roleDetails.isJsonObject()) {
            JsonObject obj = roleDetails.getAsJsonObject();
            if (obj.has("rejectionReason") && !obj.get("rejectionReason").isJsonNull()) {
                return obj.get("rejectionReason").getAsString();
            }
        }
        return null;
    }
}
