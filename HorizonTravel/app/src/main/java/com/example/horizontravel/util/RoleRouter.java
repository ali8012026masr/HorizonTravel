package com.example.horizontravel.util;

import android.content.Context;
import android.content.Intent;

import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.ui.admin.AdminMainActivity;
import com.example.horizontravel.ui.guide.GuideMainActivity;
import com.example.horizontravel.ui.provider.ProviderMainActivity;
import com.example.horizontravel.ui.tourist.TouristMainActivity;

public final class RoleRouter {
    private RoleRouter() {}

    public static Intent shellIntentFor(Context context, String role) {
        if (role == null) {
            return new Intent(context, TouristMainActivity.class);
        }
        switch (role) {
            case Constants.Role.TOUR_GUIDE:
                return new Intent(context, GuideMainActivity.class);
            case Constants.Role.SERVICE_PROVIDER:
                return new Intent(context, ProviderMainActivity.class);
            case Constants.Role.ADMIN:
                return new Intent(context, AdminMainActivity.class);
            case Constants.Role.TOURIST:
            default:
                return new Intent(context, TouristMainActivity.class);
        }
    }
}
