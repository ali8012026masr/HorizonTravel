package com.example.horizontravel.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.horizontravel.data.model.LoginResponse;

public class SessionManager {
    private static final String PREF_NAME = "horizon_travel_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_STATUS = "status";
    private static final String KEY_VERIFICATION_STATUS = "verification_status";
    private static final String KEY_REJECTION_REASON = "rejection_reason";
    private static final String KEY_EXPIRES_IN_SECONDS = "expires_in_seconds";
    private static final String KEY_THEME_MODE = "theme_mode";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager has not been initialized with Application context");
        }
        return instance;
    }

    public void save(LoginResponse response) {
        SharedPreferences.Editor editor = prefs.edit();
        if (response.getAccessToken() != null) {
            editor.putString(KEY_ACCESS_TOKEN, response.getAccessToken());
        }
        if (response.getRefreshToken() != null) {
            editor.putString(KEY_REFRESH_TOKEN, response.getRefreshToken());
        }
        if (response.getId() != null) {
            editor.putLong(KEY_USER_ID, response.getId());
        }
        editor.putString(KEY_FULL_NAME, response.getFullName());
        editor.putString(KEY_EMAIL, response.getEmail());
        editor.putString(KEY_ROLE, response.getRole());

        String status = response.getStatus();
        if (status != null) {
            editor.putString(KEY_STATUS, status);
        } else {
            editor.remove(KEY_STATUS);
        }

        String verificationStatus = response.getVerificationStatus();
        if (verificationStatus != null) {
            editor.putString(KEY_VERIFICATION_STATUS, verificationStatus);
        } else {
            editor.remove(KEY_VERIFICATION_STATUS);
        }

        String rejectionReason = response.getRejectionReason();
        if (rejectionReason != null) {
            editor.putString(KEY_REJECTION_REASON, rejectionReason);
        } else {
            editor.remove(KEY_REJECTION_REASON);
        }

        if (response.getExpiresInSeconds() != null) {
            editor.putLong(KEY_EXPIRES_IN_SECONDS, response.getExpiresInSeconds());
        }

        editor.apply();
    }

    public void updateAccessToken(String newAccessToken) {
        if (newAccessToken != null && !newAccessToken.isEmpty()) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, newAccessToken).apply();
        }
    }

    public void updateStatusAndVerification(String status, String verificationStatus, String rejectionReason) {
        SharedPreferences.Editor editor = prefs.edit();
        if (status != null) {
            editor.putString(KEY_STATUS, status);
        }
        if (verificationStatus != null) {
            editor.putString(KEY_VERIFICATION_STATUS, verificationStatus);
        }
        if (rejectionReason != null) {
            editor.putString(KEY_REJECTION_REASON, rejectionReason);
        } else {
            editor.remove(KEY_REJECTION_REASON);
        }
        editor.apply();
    }

    public boolean isLoggedIn() {
        String token = prefs.getString(KEY_ACCESS_TOKEN, null);
        return token != null && !token.trim().isEmpty();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public void setFullName(String fullName) {
        prefs.edit().putString(KEY_FULL_NAME, fullName).apply();
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public String getStatus() {
        return prefs.getString(KEY_STATUS, null);
    }

    public String getVerificationStatus() {
        return prefs.getString(KEY_VERIFICATION_STATUS, null);
    }

    public String getRejectionReason() {
        return prefs.getString(KEY_REJECTION_REASON, null);
    }

    public long getExpiresInSeconds() {
        return prefs.getLong(KEY_EXPIRES_IN_SECONDS, -1L);
    }

    public void clear() {
        // Keep theme mode when clearing session
        int themeMode = getThemeMode();
        prefs.edit().clear().apply();
        setThemeMode(themeMode);
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }
}
