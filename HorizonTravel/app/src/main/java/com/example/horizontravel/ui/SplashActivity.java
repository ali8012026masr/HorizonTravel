package com.example.horizontravel.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.example.horizontravel.MainActivity;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.databinding.ActivitySplashBinding;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.auth.PendingVerificationActivity;
import com.example.horizontravel.util.RoleRouter;
import com.example.horizontravel.util.SessionManager;

public class SplashActivity extends BaseActivity {
    private static final long SPLASH_DELAY_MS = 600L;
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler(Looper.getMainLooper()).postDelayed(this::routeNext, SPLASH_DELAY_MS);
    }

    private void routeNext() {
        if (!isSafe()) {
            return;
        }

        SessionManager sessionManager = SessionManager.getInstance();
        if (!sessionManager.isLoggedIn()) {
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            finish();
            return;
        }

        String role = sessionManager.getRole();
        String status = sessionManager.getStatus();
        String verificationStatus = sessionManager.getVerificationStatus();

        boolean isAwaitingVerification = false;
        if (status != null && !Constants.UserStatus.ACTIVE.equalsIgnoreCase(status)) {
            isAwaitingVerification = true;
        } else if (Constants.Role.TOUR_GUIDE.equalsIgnoreCase(role) || Constants.Role.SERVICE_PROVIDER.equalsIgnoreCase(role)) {
            if (verificationStatus != null && !Constants.VerificationStatus.APPROVED.equalsIgnoreCase(verificationStatus)) {
                isAwaitingVerification = true;
            }
        }

        Intent nextIntent;
        if (isAwaitingVerification) {
            nextIntent = new Intent(this, PendingVerificationActivity.class);
        } else {
            nextIntent = RoleRouter.shellIntentFor(this, role);
        }

        startActivity(nextIntent);
        finish();
    }
}
