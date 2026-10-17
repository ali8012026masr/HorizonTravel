package com.example.horizontravel.ui.provider;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.FragmentProviderSlotsBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ProviderSlotsFragment extends BaseFragment<FragmentProviderSlotsBinding> {

    private ProviderSlotAdapter adapter;
    private final List<ServiceSlot> allSlots = new ArrayList<>();
    private String selectedStatusFilter = null; // null means All

    @Override
    protected FragmentProviderSlotsBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProviderSlotsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilters();
        binding.etSearchSlots.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadSlots);

        binding.fabAddSlot.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(requireContext(), CreateSlotActivity.class));
            }
        });

        loadSlots();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSlots();
    }

    private void setupRecyclerView() {
        adapter = new ProviderSlotAdapter(slot -> {
            if (isSingleClick() && slot != null) {
                Intent intent = new Intent(requireContext(), EditSlotActivity.class);
                intent.putExtra("EXTRA_SLOT_ID", slot.getId());
                intent.putExtra("EXTRA_CATEGORY", slot.getCategory());
                intent.putExtra("EXTRA_ORIGIN", slot.getOrigin());
                intent.putExtra("EXTRA_DESTINATION", slot.getDestination());
                intent.putExtra("EXTRA_LOCATION_NAME", slot.getLocationName());
                intent.putExtra("EXTRA_START_DATE_TIME", slot.getStartDateTime());
                intent.putExtra("EXTRA_END_DATE_TIME", slot.getEndDateTime());
                intent.putExtra("EXTRA_CAPACITY", slot.getCapacity() != null ? slot.getCapacity() : 0);
                intent.putExtra("EXTRA_PRICE", slot.getPrice());
                intent.putExtra("EXTRA_STATUS", slot.getStatus());
                startActivity(intent);
            }
        });
        binding.rvSlots.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSlots.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipOpen) {
                selectedStatusFilter = Constants.SlotStatus.OPEN;
            } else if (checkedId == R.id.chipFull) {
                selectedStatusFilter = Constants.SlotStatus.FULL;
            } else if (checkedId == R.id.chipClosed) {
                selectedStatusFilter = Constants.SlotStatus.CLOSED;
            } else if (checkedId == R.id.chipCancelled) {
                selectedStatusFilter = Constants.SlotStatus.CANCELLED;
            } else {
                selectedStatusFilter = null;
            }
            applyFilter();
        });
    }

    private void loadSlots() {
        long providerId = SessionManager.getInstance().getUserId();
        if (providerId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);

        ApiClient.getApiService().getSlotsForProvider(providerId).enqueue(new ApiCallback<List<ServiceSlot>>() {
            @Override
            public void onSuccess(List<ServiceSlot> slots) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                allSlots.clear();
                if (slots != null) {
                    allSlots.addAll(slots);
                }
                applyFilter();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                showError(message);
                updateEmptyState(adapter.getItemCount() == 0);
            }
        });
    }

    private void applyFilter() {
        List<ServiceSlot> filtered = new ArrayList<>();
        for (ServiceSlot slot : allSlots) {
            String term = binding.etSearchSlots.getText() == null ? "" :
                    binding.etSearchSlots.getText().toString().trim().toLowerCase(java.util.Locale.US);
            String searchable = ((slot.getCategory() == null ? "" : slot.getCategory()) + " "
                    + (slot.getOrigin() == null ? "" : slot.getOrigin()) + " "
                    + (slot.getDestination() == null ? "" : slot.getDestination()) + " "
                    + (slot.getLocationName() == null ? "" : slot.getLocationName())).toLowerCase(java.util.Locale.US);
            if ((selectedStatusFilter == null || selectedStatusFilter.equalsIgnoreCase(slot.getStatus()))
                    && (term.isEmpty() || searchable.contains(term))) {
                filtered.add(slot);
            }
        }
        adapter.setItems(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvSlots.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
