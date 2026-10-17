package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.data.model.Tourist;
import com.example.horizontravel.databinding.FragmentHomeBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.SessionManager;

import java.util.List;

public class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    private SlotAdapter slotAdapter;

    @Override
    protected FragmentHomeBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViews();
        setupRecyclerView();
        setupListeners();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    private void setupViews() {
        String fullName = SessionManager.getInstance().getFullName();
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Traveler";
        }
        binding.tvGreeting.setText(getString(R.string.home_greeting, fullName));
        binding.tvLoyaltyPoints.setText(getString(R.string.home_points_balance, 0));
    }

    private void setupRecyclerView() {
        slotAdapter = new SlotAdapter(slot -> {
            if (slot != null && slot.getId() != null) {
                Intent intent = new Intent(requireContext(), SlotDetailActivity.class);
                intent.putExtra(SlotDetailActivity.EXTRA_SLOT_ID, slot.getId());
                startActivity(intent);
            }
        });
        binding.rvPopularSlots.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPopularSlots.setAdapter(slotAdapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadData);

        binding.cardAiConcierge.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AiConciergeActivity.class));
        });

        binding.cardGroups.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), GroupsActivity.class));
        });

        binding.cardGuides.setOnClickListener(v -> {
            if (getActivity() instanceof TouristMainActivity) {
                ((TouristMainActivity) getActivity()).navigateToGuides();
            }
        });

        binding.cardExplore.setOnClickListener(v -> {
            if (getActivity() instanceof TouristMainActivity) {
                ((TouristMainActivity) getActivity()).selectTab(R.id.nav_explore);
            }
        });

        binding.chipHotel.setOnClickListener(v -> navigateCategory("HOTEL"));
        binding.chipResort.setOnClickListener(v -> navigateCategory("RESORT"));
        binding.chipBus.setOnClickListener(v -> navigateCategory("BUS"));
        binding.chipTrain.setOnClickListener(v -> navigateCategory("TRAIN"));
        binding.chipAirplane.setOnClickListener(v -> navigateCategory("AIRPLANE"));
        binding.chipAttraction.setOnClickListener(v -> navigateCategory(Constants.ServiceCategory.AMUSEMENT_PARK));
        binding.chipBuffet.setOnClickListener(v -> navigateCategory("BUFFET"));
    }

    private void navigateCategory(String category) {
        if (getActivity() instanceof TouristMainActivity) {
            ((TouristMainActivity) getActivity()).navigateToExplore(category, null, null, null);
        }
    }

    private void loadData() {
        loadProfile();
        loadPopularSlots();
    }

    private void loadProfile() {
        long userId = SessionManager.getInstance().getUserId();
        if (userId <= 0) return;

        ApiClient.getApiService().getTouristProfile(userId).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist tourist) {
                if (!isSafe()) return;
                binding.swipeRefresh.setRefreshing(false);
                if (tourist != null) {
                    if (tourist.getFullName() != null) {
                        binding.tvGreeting.setText(getString(R.string.home_greeting, tourist.getFullName()));
                        SessionManager.getInstance().setFullName(tourist.getFullName());
                    }
                    binding.tvLoyaltyPoints.setText(
                            getString(R.string.home_points_balance, tourist.getLoyaltyPoints()));
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void loadPopularSlots() {
        binding.progressBarSlots.setVisibility(View.VISIBLE);
        ApiClient.getApiService().searchSlots(null, null, null, null, null, null, null, null)
                .enqueue(new ApiCallback<List<ServiceSlot>>() {
                    @Override
                    public void onSuccess(List<ServiceSlot> slots) {
                        if (!isSafe()) return;
                        binding.progressBarSlots.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        slotAdapter.setSlots(slots);
                    }

                    @Override
                    public void onError(String message, int statusCode) {
                        if (!isSafe()) return;
                        binding.progressBarSlots.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                    }
                });
    }
}
