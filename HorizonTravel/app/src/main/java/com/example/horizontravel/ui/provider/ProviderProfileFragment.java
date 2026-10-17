package com.example.horizontravel.ui.provider;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Provider;
import com.example.horizontravel.databinding.FragmentProviderProfileBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.shared.ChangePasswordActivity;
import com.example.horizontravel.ui.shared.NotificationsActivity;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class ProviderProfileFragment extends BaseFragment<FragmentProviderProfileBinding> {

    private Provider currentProvider;

    @Override
    protected FragmentProviderProfileBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProviderProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.swipeRefresh.setOnRefreshListener(this::loadAllData);

        binding.btnEditProfile.setOnClickListener(v -> {
            if (isSingleClick() && currentProvider != null) {
                Intent intent = new Intent(requireContext(), EditProviderProfileActivity.class);
                intent.putExtra("EXTRA_FULL_NAME", currentProvider.getFullName());
                intent.putExtra("EXTRA_BUSINESS_NAME", currentProvider.getBusinessName());
                intent.putExtra("EXTRA_TRADE_LICENSE", currentProvider.getTradeLicenseNo());
                intent.putExtra("EXTRA_CATEGORY", currentProvider.getCategory());
                intent.putExtra("EXTRA_PHONE", currentProvider.getPhone());
                intent.putExtra("EXTRA_ADDRESS", currentProvider.getAddress());
                if (currentProvider.getCommissionRate() != null) {
                    intent.putExtra("EXTRA_COMMISSION_RATE", currentProvider.getCommissionRate());
                }
                startActivity(intent);
            }
        });

        binding.btnNotifications.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(requireContext(), NotificationsActivity.class));
            }
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
            }
        });

        binding.btnLogout.setOnClickListener(v -> {
            if (isSingleClick()) {
                confirmLogout();
            }
        });

        loadAllData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllData();
    }

    private void loadAllData() {
        long providerId = SessionManager.getInstance().getUserId();
        if (providerId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        // 1. Load Profile
        ApiClient.getApiService().getProviderById(providerId).enqueue(new ApiCallback<Provider>() {
            @Override
            public void onSuccess(Provider provider) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                if (provider != null) {
                    currentProvider = provider;
                    bindProvider(provider);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                showError(message);
            }
        });

    }

    private void bindProvider(Provider provider) {
        String business = provider.getBusinessName();
        binding.tvBusinessName.setText(business != null && !business.trim().isEmpty() ? business : provider.getFullName());

        String status = provider.getVerificationStatus() != null ? provider.getVerificationStatus() : "APPROVED";
        binding.tvVerificationStatus.setText(status);

        String owner = provider.getFullName();
        binding.tvOwnerName.setText(String.format(java.util.Locale.US, "Owner: %s", owner != null ? owner : ""));

        String category = provider.getCategory();
        binding.tvCategory.setText(getString(R.string.provider_category_label,
                category != null ? Formatters.capitalizeEnum(category) : "N/A"));

        String license = provider.getTradeLicenseNo();
        binding.tvTradeLicense.setText(getString(R.string.provider_license_label,
                license != null && !license.trim().isEmpty() ? license : "N/A"));

        double rate = provider.getCommissionRate() != null ? provider.getCommissionRate() : 0.0;
        binding.tvCommissionRate.setText(getString(R.string.provider_commission_rate_label, rate));

        StringBuilder contact = new StringBuilder();
        if (provider.getEmail() != null) contact.append("Email: ").append(provider.getEmail()).append("\n");
        if (provider.getPhone() != null) contact.append("Phone: ").append(provider.getPhone()).append("\n");
        if (provider.getAddress() != null) contact.append("Address: ").append(provider.getAddress());
        binding.tvContactInfo.setText(contact.toString().trim());

        double avg = provider.getRatingAvg() != null ? provider.getRatingAvg() : 0.0;
        int count = provider.getRatingCount() != null ? provider.getRatingCount() : 0;
        binding.ratingBar.setRating((float) avg);
        if (count > 0) {
            binding.tvRatingText.setText(getString(R.string.provider_rating_label, avg, count));
        } else {
            binding.tvRatingText.setText(R.string.provider_no_rating);
        }
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    SessionManager.getInstance().clear();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
