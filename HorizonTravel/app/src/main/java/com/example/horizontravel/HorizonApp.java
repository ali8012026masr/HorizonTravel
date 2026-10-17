package com.example.horizontravel;

import android.app.Application;

import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.util.SessionManager;

public class HorizonApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(SessionManager.getInstance().getThemeMode());
        com.example.horizontravel.util.CartManager.init(this);
        ApiClient.init();
    }
}
