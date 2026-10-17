package com.example.horizontravel.ui.admin;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.PendingRegistrationsResponse;
import com.example.horizontravel.data.model.Provider;
import com.example.horizontravel.data.model.VerifyRequest;
import com.example.horizontravel.databinding.FragmentVerificationsBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.ArrayList;

public class VerificationsFragment extends BaseFragment<FragmentVerificationsBinding> {

    private PendingGuideAdapter guideAdapter;
    private PendingProviderAdapter providerAdapter;
    private List<Guide> guides = new ArrayList<>();
    private List<Provider> providers = new ArrayList<>();
    private int selectedFilter = R.id.chipVerificationAll;

    @Override
    protected FragmentVerificationsBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentVerificationsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapters();
        setupFilters();
        binding.etSearchVerifications.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadPendingRegistrations);

        loadPendingRegistrations();
    }

    private void setupFilters() {
        binding.chipGroupVerificationType.setOnCheckedChangeListener((group, checkedId) -> {
            selectedFilter = checkedId;
            applyFilter();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPendingRegistrations();
    }

    private void setupAdapters() {
        guideAdapter = new PendingGuideAdapter(new PendingGuideAdapter.OnPendingGuideActionListener() {
            @Override
            public void onApprove(Guide guide) {
                if (isSingleClick() && guide != null) {
                    confirmApproveGuide(guide);
                }
            }

            @Override
            public void onReject(Guide guide) {
                if (isSingleClick() && guide != null) {
                    promptRejectGuide(guide);
                }
            }

            @Override
            public void onDetails(Guide guide) {
                openDetails("guide", guide);
            }
        });
        binding.rvGuides.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGuides.setAdapter(guideAdapter);

        providerAdapter = new PendingProviderAdapter(new PendingProviderAdapter.OnPendingProviderActionListener() {
            @Override
            public void onApprove(Provider provider) {
                if (isSingleClick() && provider != null) {
                    confirmApproveProvider(provider);
                }
            }

            @Override
            public void onReject(Provider provider) {
                if (isSingleClick() && provider != null) {
                    promptRejectProvider(provider);
                }
            }

            @Override
            public void onDetails(Provider provider) {
                openDetails("provider", provider);
            }
        });
        binding.rvProviders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProviders.setAdapter(providerAdapter);
    }

    private void loadPendingRegistrations() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyVerifications.setVisibility(View.GONE);

        ApiClient.getApiService().getPendingRegistrations().enqueue(new ApiCallback<PendingRegistrationsResponse>() {
            @Override
            public void onSuccess(PendingRegistrationsResponse response) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                guides = response != null && response.getGuides() != null
                        ? new ArrayList<>(response.getGuides()) : new ArrayList<>();
                providers = response != null && response.getProviders() != null
                        ? new ArrayList<>(response.getProviders()) : new ArrayList<>();
                applyFilter();
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

    private void applyFilter() {
        if (!isSafe()) return;
        boolean showGuides = selectedFilter == R.id.chipVerificationAll
                || selectedFilter == R.id.chipVerificationGuides;
        boolean showProviders = selectedFilter == R.id.chipVerificationAll
                || selectedFilter == R.id.chipVerificationProviders;
        int guideCount = showGuides ? guides.size() : 0;
        int providerCount = showProviders ? providers.size() : 0;

        String term = binding.etSearchVerifications.getText() == null ? "" :
                binding.etSearchVerifications.getText().toString().trim().toLowerCase(java.util.Locale.US);
        List<Guide> filteredGuides = new ArrayList<>();
        for (Guide guide : guides) {
            String searchable = ((guide.getFullName() == null ? "" : guide.getFullName()) + " "
                    + (guide.getEmail() == null ? "" : guide.getEmail()) + " "
                    + (guide.getLocation() == null ? "" : guide.getLocation())).toLowerCase(java.util.Locale.US);
            if (term.isEmpty() || searchable.contains(term)) filteredGuides.add(guide);
        }
        List<Provider> filteredProviders = new ArrayList<>();
        for (Provider provider : providers) {
            String searchable = ((provider.getFullName() == null ? "" : provider.getFullName()) + " "
                    + (provider.getBusinessName() == null ? "" : provider.getBusinessName()) + " "
                    + (provider.getEmail() == null ? "" : provider.getEmail()) + " "
                    + (provider.getCategory() == null ? "" : provider.getCategory())).toLowerCase(java.util.Locale.US);
            if (term.isEmpty() || searchable.contains(term)) filteredProviders.add(provider);
        }
        guideCount = showGuides ? filteredGuides.size() : 0;
        providerCount = showProviders ? filteredProviders.size() : 0;

        binding.tvGuidesHeader.setText(String.format(java.util.Locale.US, "Tour Guides (%d)", guideCount));
        guideAdapter.setItems(showGuides ? filteredGuides : new ArrayList<>());
        binding.tvNoGuides.setVisibility(showGuides && guideCount == 0 ? View.VISIBLE : View.GONE);
        binding.layoutGuidesSection.setVisibility(showGuides ? View.VISIBLE : View.GONE);
        binding.rvGuides.setVisibility(guideCount > 0 ? View.VISIBLE : View.GONE);

        binding.tvProvidersHeader.setText(String.format(java.util.Locale.US, "Service Providers (%d)", providerCount));
        providerAdapter.setItems(showProviders ? filteredProviders : new ArrayList<>());
        binding.tvNoProviders.setVisibility(showProviders && providerCount == 0 ? View.VISIBLE : View.GONE);
        binding.layoutProvidersSection.setVisibility(showProviders ? View.VISIBLE : View.GONE);
        binding.rvProviders.setVisibility(providerCount > 0 ? View.VISIBLE : View.GONE);

        binding.sectionDivider.setVisibility(showGuides && showProviders ? View.VISIBLE : View.GONE);
        binding.layoutEmptyVerifications.setVisibility(guideCount == 0 && providerCount == 0
                ? View.VISIBLE : View.GONE);
    }

    private void openDetails(String role, Object user) {
        if (!isSafe() || user == null || !isSingleClick()) return;
        Intent intent = new Intent(requireContext(), VerificationDetailActivity.class);
        intent.putExtra(VerificationDetailActivity.EXTRA_ROLE, role);
        intent.putExtra(VerificationDetailActivity.EXTRA_JSON,
                new com.google.gson.Gson().toJson(user));
        startActivity(intent);
    }

    // --- Guide Actions ---

    private void confirmApproveGuide(Guide guide) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_approve_title)
                .setMessage(getString(R.string.confirm_approve_guide_msg, guide.getFullName()))
                .setPositiveButton(R.string.btn_approve, (dialog, which) -> {
                    verifyGuide(guide.getId(), true, null);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void promptRejectGuide(Guide guide) {
        final EditText input = new EditText(requireContext());
        input.setHint(R.string.dialog_reject_reason_hint);
        FrameLayout container = new FrameLayout(requireContext());
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 0, padding, 0);
        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_reject_title)
                .setView(container)
                .setPositiveButton(R.string.btn_confirm_reject, (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        showToast(getString(R.string.dialog_reject_reason_hint));
                        return;
                    }
                    verifyGuide(guide.getId(), false, reason);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void verifyGuide(long guideId, boolean approved, String reason) {
        binding.progressBar.setVisibility(View.VISIBLE);
        VerifyRequest request = new VerifyRequest(approved, reason);

        ApiClient.getApiService().verifyGuide(guideId, request).enqueue(new ApiCallback<Guide>() {
            @Override
            public void onSuccess(Guide guide) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                showToast(getString(approved ? R.string.approval_success : R.string.rejection_success));
                loadPendingRegistrations();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    // --- Provider Actions ---

    private void confirmApproveProvider(Provider provider) {
        String name = provider.getBusinessName() != null ? provider.getBusinessName() : provider.getFullName();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_approve_title)
                .setMessage(getString(R.string.confirm_approve_provider_msg, name))
                .setPositiveButton(R.string.btn_approve, (dialog, which) -> {
                    verifyProvider(provider.getId(), true, null);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void promptRejectProvider(Provider provider) {
        final EditText input = new EditText(requireContext());
        input.setHint(R.string.dialog_reject_reason_hint);
        FrameLayout container = new FrameLayout(requireContext());
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 0, padding, 0);
        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_reject_title)
                .setView(container)
                .setPositiveButton(R.string.btn_confirm_reject, (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        showToast(getString(R.string.dialog_reject_reason_hint));
                        return;
                    }
                    verifyProvider(provider.getId(), false, reason);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void verifyProvider(long providerId, boolean approved, String reason) {
        binding.progressBar.setVisibility(View.VISIBLE);
        VerifyRequest request = new VerifyRequest(approved, reason);

        ApiClient.getApiService().verifyProvider(providerId, request).enqueue(new ApiCallback<Provider>() {
            @Override
            public void onSuccess(Provider provider) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                showToast(getString(approved ? R.string.approval_success : R.string.rejection_success));
                loadPendingRegistrations();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                showError(message);
            }
        });
    }
}
