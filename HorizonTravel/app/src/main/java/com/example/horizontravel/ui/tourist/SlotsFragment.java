package com.example.horizontravel.ui.tourist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.FragmentSlotsBinding;
import com.example.horizontravel.ui.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SlotsFragment extends BaseFragment<FragmentSlotsBinding> {

    private SlotAdapter slotAdapter;
    private final List<String> categoryList = new ArrayList<>();
    private final List<ServiceSlot> allLoadedSlots = new ArrayList<>();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String currentSearchQuery = "";
    private boolean isSpinnerInitialized = false;

    private String pendingCategory;
    private Double pendingMinPrice;
    private Double pendingMaxPrice;
    private String pendingQuery;

    @Override
    protected FragmentSlotsBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSlotsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCategories();
        setupRecyclerView();
        setupListeners();

        if (pendingCategory != null || pendingQuery != null || pendingMaxPrice != null) {
            applyPendingFilters();
        } else {
            loadSlots();
        }
    }

    public void applyFilters(String category, Double minPrice, Double maxPrice, String query) {
        this.pendingCategory = category;
        this.pendingMinPrice = minPrice;
        this.pendingMaxPrice = maxPrice;
        this.pendingQuery = query;

        if (isAdded() && binding != null) {
            applyPendingFilters();
        }
    }

    private void applyPendingFilters() {
        if (binding == null) return;

        if (pendingQuery != null && binding.etSearch != null) {
            binding.etSearch.setText(pendingQuery);
            currentSearchQuery = pendingQuery.trim();
        }
        if (pendingMaxPrice != null && binding.etMaxPrice != null) {
            binding.etMaxPrice.setText(String.valueOf(pendingMaxPrice));
        }
        if (pendingCategory != null && binding.spinnerCategory != null) {
            String targetCat = "ATTRACTION".equalsIgnoreCase(pendingCategory)
                    ? Constants.ServiceCategory.AMUSEMENT_PARK
                    : pendingCategory;
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).equalsIgnoreCase(targetCat)) {
                    binding.spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
        pendingCategory = null;
        pendingMinPrice = null;
        pendingMaxPrice = null;
        pendingQuery = null;
        loadSlots();
    }

    private void setupCategories() {
        categoryList.clear();
        categoryList.add("All Categories");
        categoryList.add(Constants.ServiceCategory.HOTEL);
        categoryList.add(Constants.ServiceCategory.RESORT);
        categoryList.add(Constants.ServiceCategory.BUS);
        categoryList.add(Constants.ServiceCategory.TRAIN);
        categoryList.add(Constants.ServiceCategory.AIRPLANE);
        categoryList.add(Constants.ServiceCategory.SHIP);
        categoryList.add(Constants.ServiceCategory.MICROBUS);
        categoryList.add(Constants.ServiceCategory.LAUNCH);
        categoryList.add(Constants.ServiceCategory.AMUSEMENT_PARK);
        categoryList.add(Constants.ServiceCategory.BUFFET);
        categoryList.add(Constants.ServiceCategory.CONVENTION_CENTER);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categoryList
        );
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        slotAdapter = new SlotAdapter(slot -> {
            if (slot != null && slot.getId() != null) {
                Intent intent = new Intent(requireContext(), SlotDetailActivity.class);
                intent.putExtra(SlotDetailActivity.EXTRA_SLOT_ID, slot.getId());
                startActivity(intent);
            }
        });
        binding.rvSlots.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSlots.setAdapter(slotAdapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadSlots);

        binding.btnApplyFilter.setOnClickListener(v -> {
            hideKeyboard();
            loadSlots();
        });

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true;
                    return;
                }
                loadSlots();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                currentSearchQuery = s != null ? s.toString().trim() : "";
                searchRunnable = () -> filterAndDisplaySlots();
                searchHandler.postDelayed(searchRunnable, 200);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                filterAndDisplaySlots();
                return true;
            }
            return false;
        });

        binding.btnRetry.setOnClickListener(v -> loadSlots());
    }

    private void loadSlots() {
        if (!isSafe() || binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        String category = null;
        int selectedIndex = binding.spinnerCategory.getSelectedItemPosition();
        if (selectedIndex > 0 && selectedIndex < categoryList.size()) {
            category = categoryList.get(selectedIndex);
            if ("ATTRACTION".equalsIgnoreCase(category)) {
                category = Constants.ServiceCategory.AMUSEMENT_PARK;
            }
        }

        Double maxPrice = null;
        if (binding.etMaxPrice.getText() != null) {
            String priceStr = binding.etMaxPrice.getText().toString().trim();
            if (!priceStr.isEmpty()) {
                try {
                    maxPrice = Double.parseDouble(priceStr);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (binding.etSearch.getText() != null) {
            currentSearchQuery = binding.etSearch.getText().toString().trim();
        }

        // Fetch slots filtered by category and maxPrice from server.
        // Origin/destination/service name search is handled client-side
        // to support multi-field querying across origin, destination, provider, and service name.
        ApiClient.getApiService().searchSlots(
                category,
                null,
                null,
                null,
                null,
                null,
                null,
                maxPrice
        ).enqueue(new ApiCallback<List<ServiceSlot>>() {
            @Override
            public void onSuccess(List<ServiceSlot> slots) {
                if (!isSafe() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                allLoadedSlots.clear();
                if (slots != null) {
                    for (ServiceSlot s : slots) {
                        if (s != null && !"CANCELLED".equalsIgnoreCase(s.getStatus())) {
                            allLoadedSlots.add(s);
                        }
                    }
                }
                filterAndDisplaySlots();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe() || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (allLoadedSlots.isEmpty()) {
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_generic));
                    binding.rvSlots.setVisibility(View.GONE);
                } else {
                    showToast(message != null ? message : getString(R.string.error_generic));
                }
            }
        });
    }

    private void filterAndDisplaySlots() {
        if (!isSafe() || binding == null) return;

        List<ServiceSlot> filtered = new ArrayList<>();
        String query = currentSearchQuery;

        Double maxPrice = null;
        if (binding.etMaxPrice.getText() != null) {
            String priceStr = binding.etMaxPrice.getText().toString().trim();
            if (!priceStr.isEmpty()) {
                try {
                    maxPrice = Double.parseDouble(priceStr);
                } catch (NumberFormatException ignored) {}
            }
        }

        for (ServiceSlot slot : allLoadedSlots) {
            if (maxPrice != null && slot.getPrice() > maxPrice) {
                continue;
            }
            if (matchesQuery(slot, query)) {
                filtered.add(slot);
            }
        }

        if (filtered.isEmpty()) {
            slotAdapter.setSlots(new ArrayList<>());
            binding.rvSlots.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.rvSlots.setVisibility(View.VISIBLE);
            slotAdapter.setSlots(filtered);
        }
    }

    private boolean matchesQuery(ServiceSlot slot, String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        if (slot == null) {
            return false;
        }

        String origin = slot.getOrigin() != null ? slot.getOrigin().toLowerCase(Locale.US) : "";
        String destination = slot.getDestination() != null ? slot.getDestination().toLowerCase(Locale.US) : "";
        String locationName = slot.getLocationName() != null ? slot.getLocationName().toLowerCase(Locale.US) : "";
        String providerName = slot.getProviderBusinessName() != null ? slot.getProviderBusinessName().toLowerCase(Locale.US) : "";
        String category = slot.getCategory() != null ? slot.getCategory().toLowerCase(Locale.US) : "";
        String categoryAliases = "";
        if ("amusement_park".equals(category) || "amusement park".equals(category)) {
            categoryAliases = "attraction attractions theme park amusement park";
        }

        String combined = (origin + " " + destination + " " + origin + " to " + destination + " "
                + locationName + " " + providerName + " " + category + " " + categoryAliases).toLowerCase(Locale.US);
        String normalizedCombined = combined.replace("'", "");

        String[] tokens = query.toLowerCase(Locale.US).trim().split("\\s+");
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            String normalizedToken = token.replace("'", "");
            if (!combined.contains(token) && !normalizedCombined.contains(normalizedToken)) {
                return false;
            }
        }
        return true;
    }

    private void hideKeyboard() {
        if (binding != null && binding.etSearch != null && isAdded()) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(binding.etSearch.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        searchHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }
}
