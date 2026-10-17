package com.example.horizontravel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.ActivityMainBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.auth.RegisterChooserActivity;
import com.example.horizontravel.ui.tourist.SlotAdapter;
import com.example.horizontravel.ui.tourist.SlotDetailActivity;
import com.example.horizontravel.util.RoleRouter;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private SlotAdapter slotAdapter;
    private String selectedCategory = null;
    private String currentSearchQuery = "";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private final List<ServiceSlot> allLoadedSlots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupCategoryChips();
        setupSearch();
        setupRecyclerView();
        setupButtons();

        loadSlots();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAuthUi();
    }

    private void updateAuthUi() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            binding.btnToolbarSignIn.setVisibility(View.GONE);
            binding.btnToolbarRegister.setText(R.string.my_portal);
            binding.cardBottomCta.setVisibility(View.GONE);
        } else {
            binding.btnToolbarSignIn.setVisibility(View.VISIBLE);
            binding.btnToolbarRegister.setText(R.string.landing_register);
            binding.cardBottomCta.setVisibility(View.VISIBLE);
        }
    }

    private void setupToolbar() {
        binding.btnToolbarSignIn.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        binding.btnToolbarRegister.setOnClickListener(v -> {
            if (isSingleClick()) {
                SessionManager session = SessionManager.getInstance();
                if (session.isLoggedIn()) {
                    String role = session.getRole();
                    startActivity(RoleRouter.shellIntentFor(this, role));
                } else {
                    startActivity(new Intent(this, RegisterChooserActivity.class));
                }
            }
        });

        if (binding.btnThemeToggle != null) {
            binding.btnThemeToggle.setOnClickListener(v -> {
                if (isSingleClick()) {
                    showThemeSelectionDialog();
                }
            });
        }
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            View chip = group.findViewById(checkedId);
            if (chip != null && chip.getTag() != null) {
                String tag = chip.getTag().toString();
                selectedCategory = "ALL".equalsIgnoreCase(tag) ? null : tag;
                loadSlots();
            }
        });
    }

    private void setupSearch() {
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
                searchHandler.postDelayed(searchRunnable, 300);
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
    }

    private void setupRecyclerView() {
        slotAdapter = new SlotAdapter(slot -> {
            if (slot != null && slot.getId() != null) {
                Intent intent = new Intent(MainActivity.this, SlotDetailActivity.class);
                intent.putExtra(SlotDetailActivity.EXTRA_SLOT_ID, slot.getId());
                startActivity(intent);
            }
        });
        binding.rvSlots.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSlots.setAdapter(slotAdapter);
    }

    private void setupButtons() {
        binding.swipeRefresh.setOnRefreshListener(this::loadSlots);

        binding.btnClearFilters.setOnClickListener(v -> {
            binding.etSearch.setText("");
            currentSearchQuery = "";
            binding.chipAll.setChecked(true);
            selectedCategory = null;
            loadSlots();
        });

        binding.btnRetry.setOnClickListener(v -> loadSlots());

        binding.btnCtaSignIn.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        binding.btnCtaRegister.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, RegisterChooserActivity.class));
            }
        });
    }

    private void loadSlots() {
        binding.pbLoadingSlots.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        ApiClient.getApiService().searchSlots(
                selectedCategory,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ).enqueue(new ApiCallback<List<ServiceSlot>>() {
            @Override
            public void onSuccess(List<ServiceSlot> slots) {
                if (!isSafe()) return;
                binding.pbLoadingSlots.setVisibility(View.GONE);
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
                if (!isSafe()) return;
                binding.pbLoadingSlots.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (allLoadedSlots.isEmpty()) {
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_generic));
                    binding.rvSlots.setVisibility(View.GONE);
                    binding.tvResultsCount.setVisibility(View.GONE);
                } else {
                    showToast(message != null ? message : getString(R.string.error_generic));
                }
            }
        });
    }

    private void filterAndDisplaySlots() {
        List<ServiceSlot> filtered = new ArrayList<>();
        String query = currentSearchQuery.toLowerCase(Locale.US);

        for (ServiceSlot slot : allLoadedSlots) {
            if (matchesQuery(slot, query)) {
                filtered.add(slot);
            }
        }

        binding.tvResultsCount.setVisibility(View.VISIBLE);
        binding.tvResultsCount.setText(getString(R.string.landing_services_count, filtered.size()));

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
        if (query.isEmpty()) return true;

        if (slot.getLocationName() != null && slot.getLocationName().toLowerCase(Locale.US).contains(query)) {
            return true;
        }
        if (slot.getOrigin() != null && slot.getOrigin().toLowerCase(Locale.US).contains(query)) {
            return true;
        }
        if (slot.getDestination() != null && slot.getDestination().toLowerCase(Locale.US).contains(query)) {
            return true;
        }
        if (slot.getProviderBusinessName() != null && slot.getProviderBusinessName().toLowerCase(Locale.US).contains(query)) {
            return true;
        }
        if (slot.getCategory() != null && slot.getCategory().toLowerCase(Locale.US).contains(query)) {
            return true;
        }
        if ("attraction".contains(query) && "AMUSEMENT_PARK".equalsIgnoreCase(slot.getCategory())) {
            return true;
        }
        return false;
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        searchHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}