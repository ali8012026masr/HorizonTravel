package com.example.horizontravel.ui.provider;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Provider;
import com.example.horizontravel.data.model.UpdateProviderRequest;
import com.example.horizontravel.databinding.ActivityEditProviderProfileBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

public class EditProviderProfileActivity extends BaseActivity {

    private ActivityEditProviderProfileBinding binding;

    private static final String[] CATEGORIES = new String[] {
            Constants.ServiceCategory.BUS,
            Constants.ServiceCategory.MICROBUS,
            Constants.ServiceCategory.LAUNCH,
            Constants.ServiceCategory.TRAIN,
            Constants.ServiceCategory.AIRPLANE,
            Constants.ServiceCategory.SHIP,
            Constants.ServiceCategory.HOTEL,
            Constants.ServiceCategory.RESORT,
            Constants.ServiceCategory.CONVENTION_CENTER,
            Constants.ServiceCategory.BUFFET,
            Constants.ServiceCategory.AMUSEMENT_PARK
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.horizontravel.util.SessionManager session = com.example.horizontravel.util.SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.SERVICE_PROVIDER.equalsIgnoreCase(session.getRole())) {
            finish();
            return;
        }

        binding = ActivityEditProviderProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        binding.actCategory.setAdapter(categoryAdapter);

        populateInitialData();

        binding.btnSaveProfile.setOnClickListener(v -> {
            if (isSingleClick()) {
                submitProfileUpdate();
            }
        });
    }

    private void populateInitialData() {
        binding.etFullName.setText(getIntent().getStringExtra("EXTRA_FULL_NAME"));
        binding.etBusinessName.setText(getIntent().getStringExtra("EXTRA_BUSINESS_NAME"));
        binding.etTradeLicense.setText(getIntent().getStringExtra("EXTRA_TRADE_LICENSE"));

        String category = getIntent().getStringExtra("EXTRA_CATEGORY");
        if (category != null && !category.isEmpty()) {
            binding.actCategory.setText(category, false);
        }

        binding.etPhone.setText(getIntent().getStringExtra("EXTRA_PHONE"));
        binding.etAddress.setText(getIntent().getStringExtra("EXTRA_ADDRESS"));

        if (getIntent().hasExtra("EXTRA_COMMISSION_RATE")) {
            double rate = getIntent().getDoubleExtra("EXTRA_COMMISSION_RATE", 0.0);
            binding.etCommissionRate.setText(String.valueOf(rate));
        }
    }

    private void submitProfileUpdate() {
        clearErrors();

        long providerId = SessionManager.getInstance().getUserId();
        if (providerId <= 0) {
            Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        if (fullName.isEmpty()) {
            binding.tilFullName.setError(getString(R.string.error_field_required));
            return;
        }

        String businessName = binding.etBusinessName.getText() != null ? binding.etBusinessName.getText().toString().trim() : "";
        if (businessName.isEmpty()) {
            binding.tilBusinessName.setError(getString(R.string.error_field_required));
            return;
        }

        String tradeLicense = binding.etTradeLicense.getText() != null ? binding.etTradeLicense.getText().toString().trim() : "";
        String category = binding.actCategory.getText() != null ? binding.actCategory.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "";

        Double commissionRate = null;
        String rateStr = binding.etCommissionRate.getText() != null ? binding.etCommissionRate.getText().toString().trim() : "";
        if (!rateStr.isEmpty()) {
            try {
                commissionRate = Double.parseDouble(rateStr);
            } catch (NumberFormatException ignored) {}
        }

        UpdateProviderRequest request = new UpdateProviderRequest();
        request.setFullName(fullName);
        request.setBusinessName(businessName);
        request.setTradeLicenseNo(tradeLicense.isEmpty() ? null : tradeLicense);
        request.setCategory(category.isEmpty() ? null : category);
        request.setPhone(phone.isEmpty() ? null : phone);
        request.setAddress(address.isEmpty() ? null : address);
        request.setCommissionRate(commissionRate);

        setLoading(true);

        ApiClient.getApiService().updateProviderProfile(providerId, request).enqueue(new ApiCallback<Provider>() {
            @Override
            public void onSuccess(Provider updated) {
                if (!isSafe()) return;
                setLoading(false);
                if (updated != null && updated.getFullName() != null) {
                    SessionManager.getInstance().setFullName(updated.getFullName());
                }
                Toast.makeText(EditProviderProfileActivity.this, R.string.provider_profile_updated_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                setLoading(false);
                Toast.makeText(EditProviderProfileActivity.this, message != null ? message : getString(R.string.error_network), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearErrors() {
        binding.tilFullName.setError(null);
        binding.tilBusinessName.setError(null);
        binding.tilTradeLicense.setError(null);
        binding.tilCategory.setError(null);
        binding.tilPhone.setError(null);
        binding.tilAddress.setError(null);
        binding.tilCommissionRate.setError(null);
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!loading);
    }
}
