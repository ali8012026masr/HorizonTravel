package com.example.horizontravel.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.Provider;
import com.example.horizontravel.data.model.Tourist;
import com.example.horizontravel.databinding.ActivityPendingVerificationBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.RoleRouter;
import com.example.horizontravel.util.SessionManager;

public class PendingVerificationActivity extends BaseActivity {
    private ActivityPendingVerificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPendingVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateUiFromSession();

        binding.btnRefresh.setOnClickListener(v -> {
            if (isSingleClick()) {
                refreshStatus();
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            if (isSingleClick()) {
                logout();
            }
        });
    }

    private void updateUiFromSession() {
        SessionManager session = SessionManager.getInstance();
        String status = session.getStatus();
        String verificationStatus = session.getVerificationStatus();
        String rejectionReason = session.getRejectionReason();

        if (Constants.UserStatus.SUSPENDED.equalsIgnoreCase(status)) {
            binding.tvStatusBadge.setText(Constants.UserStatus.SUSPENDED);
            binding.tvStatusBadge.setBackgroundColor(getColor(R.color.status_suspended));
            binding.tvStatusTitle.setText(R.string.status_suspended_title);
            binding.tvStatusDesc.setText(R.string.status_suspended_desc);
        } else if (Constants.UserStatus.REVOKED.equalsIgnoreCase(status)) {
            binding.tvStatusBadge.setText(Constants.UserStatus.REVOKED);
            binding.tvStatusBadge.setBackgroundColor(getColor(R.color.status_error));
            binding.tvStatusTitle.setText(R.string.status_revoked_title);
            binding.tvStatusDesc.setText(R.string.status_revoked_desc);
        } else if (Constants.VerificationStatus.REJECTED.equalsIgnoreCase(verificationStatus)) {
            binding.tvStatusBadge.setText(Constants.VerificationStatus.REJECTED);
            binding.tvStatusBadge.setBackgroundColor(getColor(R.color.status_error));
            binding.tvStatusTitle.setText(R.string.status_rejected_title);
            String reason = rejectionReason != null && !rejectionReason.isEmpty() ? rejectionReason : getString(R.string.status_rejected_reason_fallback);
            binding.tvStatusDesc.setText(getString(R.string.status_rejected_desc, reason));
        } else {
            // Pending review
            binding.tvStatusBadge.setText(Constants.VerificationStatus.PENDING);
            binding.tvStatusBadge.setBackgroundColor(getColor(R.color.status_pending));
            binding.tvStatusTitle.setText(R.string.status_awaiting_title);
            binding.tvStatusDesc.setText(R.string.status_awaiting_desc);
        }
    }

    private void refreshStatus() {
        SessionManager session = SessionManager.getInstance();
        long userId = session.getUserId();
        String role = session.getRole();

        if (userId <= 0 || role == null) {
            logout();
            return;
        }

        setLoading(true);

        if (Constants.Role.TOUR_GUIDE.equalsIgnoreCase(role)) {
            ApiClient.getApiService().getGuideById(userId).enqueue(new ApiCallback<Guide>() {
                @Override
                public void onSuccess(Guide guide) {
                    if (!isSafe()) return;
                    setLoading(false);
                    if (guide != null) {
                        handleRefreshedStatus(guide.getStatus(), guide.getVerificationStatus(), guide.getRejectionReason(), role);
                    }
                }

                @Override
                public void onError(String message, int statusCode) {
                    if (!isSafe()) return;
                    setLoading(false);
                    showToast(message);
                }
            });
        } else if (Constants.Role.SERVICE_PROVIDER.equalsIgnoreCase(role)) {
            ApiClient.getApiService().getProviderById(userId).enqueue(new ApiCallback<Provider>() {
                @Override
                public void onSuccess(Provider provider) {
                    if (!isSafe()) return;
                    setLoading(false);
                    if (provider != null) {
                        handleRefreshedStatus(provider.getStatus(), provider.getVerificationStatus(), provider.getRejectionReason(), role);
                    }
                }

                @Override
                public void onError(String message, int statusCode) {
                    if (!isSafe()) return;
                    setLoading(false);
                    showToast(message);
                }
            });
        } else {
            ApiClient.getApiService().getTouristProfile(userId).enqueue(new ApiCallback<Tourist>() {
                @Override
                public void onSuccess(Tourist tourist) {
                    if (!isSafe()) return;
                    setLoading(false);
                    if (tourist != null) {
                        handleRefreshedStatus(tourist.getStatus(), Constants.VerificationStatus.APPROVED, null, role);
                    }
                }

                @Override
                public void onError(String message, int statusCode) {
                    if (!isSafe()) return;
                    setLoading(false);
                    showToast(message);
                }
            });
        }
    }

    private void handleRefreshedStatus(String status, String verificationStatus, String rejectionReason, String role) {
        SessionManager session = SessionManager.getInstance();
        session.updateStatusAndVerification(status, verificationStatus, rejectionReason);

        boolean isApproved = Constants.UserStatus.ACTIVE.equalsIgnoreCase(status);
        if (Constants.Role.TOUR_GUIDE.equalsIgnoreCase(role) || Constants.Role.SERVICE_PROVIDER.equalsIgnoreCase(role)) {
            isApproved = isApproved && Constants.VerificationStatus.APPROVED.equalsIgnoreCase(verificationStatus);
        }

        if (isApproved) {
            showToast(getString(R.string.status_now_approved));
            Intent shellIntent = RoleRouter.shellIntentFor(this, role);
            shellIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(shellIntent);
            finish();
        } else {
            showToast(getString(R.string.status_still_pending));
            updateUiFromSession();
        }
    }

    private void logout() {
        SessionManager.getInstance().clear();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void setLoading(boolean loading) {
        binding.btnRefresh.setEnabled(!loading);
        binding.btnRefresh.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogout.setEnabled(!loading);
    }
}
