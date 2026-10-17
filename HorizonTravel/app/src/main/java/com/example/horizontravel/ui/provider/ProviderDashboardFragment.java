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
import com.example.horizontravel.data.model.Booking;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Provider;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.FragmentProviderDashboardBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.SessionManager;

import java.util.List;

public class ProviderDashboardFragment extends BaseFragment<FragmentProviderDashboardBinding> {

    @Override
    protected FragmentProviderDashboardBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProviderDashboardBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.swipeRefresh.setOnRefreshListener(this::loadDashboardData);

        binding.btnCreateSlot.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(requireContext(), CreateSlotActivity.class));
            }
        });

        binding.btnManageSlots.setOnClickListener(v -> {
            if (isSingleClick() && getActivity() instanceof ProviderMainActivity) {
                ((ProviderMainActivity) getActivity()).switchToTab(R.id.nav_provider_slots);
            }
        });

        binding.btnLookupBooking.setOnClickListener(v -> {
            if (isSingleClick() && getActivity() instanceof ProviderMainActivity) {
                ((ProviderMainActivity) getActivity()).switchToTab(R.id.nav_provider_ledger);
            }
        });

        binding.btnViewProfile.setOnClickListener(v -> {
            if (isSingleClick() && getActivity() instanceof ProviderMainActivity) {
                ((ProviderMainActivity) getActivity()).switchToTab(R.id.nav_provider_profile);
            }
        });

        loadDashboardData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        long providerId = SessionManager.getInstance().getUserId();
        if (providerId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        // 1. Load Provider Details
        ApiClient.getApiService().getProviderById(providerId).enqueue(new ApiCallback<Provider>() {
            @Override
            public void onSuccess(Provider provider) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                if (provider != null) {
                    bindProviderInfo(provider);
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

        // 2. Load Slots to count open & total
        ApiClient.getApiService().getSlotsForProvider(providerId).enqueue(new ApiCallback<List<ServiceSlot>>() {
            @Override
            public void onSuccess(List<ServiceSlot> slots) {
                if (!isSafe()) return;
                int total = slots != null ? slots.size() : 0;
                int open = 0;
                if (slots != null) {
                    for (ServiceSlot s : slots) {
                        if (Constants.SlotStatus.OPEN.equalsIgnoreCase(s.getStatus())) {
                            open++;
                        }
                    }
                }
                binding.tvTotalSlotsCount.setText(String.valueOf(total));
                binding.tvOpenSlotsCount.setText(String.valueOf(open));
            }

            @Override
            public void onError(String message, int statusCode) {
                // Non-fatal for dashboard overview
            }
        });

        // 3. Load Bookings to count pending - Gap in backend, placeholder 0
        binding.tvPendingBookingsCount.setText(R.string.text_0);
    }

    private void bindProviderInfo(Provider provider) {
        String name = provider.getBusinessName();
        if (name == null || name.trim().isEmpty()) {
            name = provider.getFullName();
        }
        if (name != null) {
            binding.tvBusinessName.setText(name);
        }

        if (provider.getCategory() != null) {
            binding.tvCategory.setText(provider.getCategory());
        }

        String status = provider.getStatus() != null ? provider.getStatus() : "ACTIVE";
        binding.tvStatusChip.setText(status);

        double avg = provider.getRatingAvg() != null ? provider.getRatingAvg() : 0.0;
        int count = provider.getRatingCount() != null ? provider.getRatingCount() : 0;

        binding.ratingBar.setRating((float) avg);
        if (count > 0) {
            binding.tvRatingSummary.setText(getString(R.string.provider_rating_summary_format, avg, count));
        } else {
            binding.tvRatingSummary.setText(R.string.provider_no_rating);
        }
    }
}
