package com.example.horizontravel.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Provider;
import com.example.horizontravel.data.model.RegisterProviderRequest;
import com.example.horizontravel.databinding.ActivityRegisterProviderBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Validators;

import java.util.Map;

public class RegisterProviderActivity extends BaseActivity {
    private ActivityRegisterProviderBinding binding;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterProviderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        binding.actCategory.setAdapter(adapter);

        binding.btnRegister.setOnClickListener(v -> {
            if (isSingleClick()) {
                performRegister();
            }
        });

        binding.tvLogin.setOnClickListener(v -> {
            if (isSingleClick()) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void performRegister() {
        clearErrors();

        String businessName = binding.etBusinessName.getText() != null ? binding.etBusinessName.getText().toString().trim() : "";
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String category = binding.actCategory.getText() != null ? binding.actCategory.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "";
        String tradeLicense = binding.etTradeLicense.getText() != null ? binding.etTradeLicense.getText().toString().trim() : "";
        String nationalId = binding.etNationalId.getText() != null ? binding.etNationalId.getText().toString().trim() : "";
        String commissionStr = binding.etCommissionRate.getText() != null ? binding.etCommissionRate.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";

        if (!Validators.isNotEmpty(businessName)) {
            binding.tilBusinessName.setError(getString(R.string.error_field_required));
            binding.etBusinessName.requestFocus();
            return;
        }

        if (!Validators.isNotEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.error_field_required));
            binding.etFullName.requestFocus();
            return;
        }

        if (!Validators.isNotEmpty(category)) {
            binding.tilCategory.setError(getString(R.string.error_field_required));
            binding.actCategory.requestFocus();
            return;
        }

        if (!Validators.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            binding.etEmail.requestFocus();
            return;
        }

        Double commissionRate = null;
        if (!commissionStr.isEmpty()) {
            try {
                commissionRate = Double.parseDouble(commissionStr);
            } catch (NumberFormatException ignored) {
            }
        }

        if (!Validators.isValidPassword(password)) {
            binding.tilPassword.setError(getString(R.string.error_password_too_short));
            binding.etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_do_not_match));
            binding.etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        RegisterProviderRequest request = new RegisterProviderRequest();
        request.setBusinessName(businessName);
        request.setFullName(fullName);
        request.setCategory(category);
        request.setEmail(email);
        request.setPassword(password);
        request.setPhone(phone.isEmpty() ? null : phone);
        request.setAddress(address.isEmpty() ? null : address);
        request.setTradeLicenseNo(tradeLicense.isEmpty() ? null : tradeLicense);
        request.setNationalId(nationalId.isEmpty() ? null : nationalId);
        request.setCommissionRate(commissionRate);

        ApiClient.getApiService().registerProvider(request).enqueue(new ApiCallback<Provider>() {
            @Override
            public void onSuccess(Provider data) {
                if (!isSafe()) return;
                setLoading(false);
                showToast(getString(R.string.registration_success_awaiting_approval));

                Intent loginIntent = new Intent(RegisterProviderActivity.this, LoginActivity.class);
                loginIntent.putExtra(LoginActivity.EXTRA_EMAIL, email);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
                finish();
            }

            @Override
            public void onError(String message, int statusCode, Map<String, String> fieldErrors) {
                if (!isSafe()) return;
                setLoading(false);

                if (statusCode == 409) {
                    binding.tilEmail.setError(getString(R.string.error_email_already_registered));
                    binding.etEmail.requestFocus();
                } else if (fieldErrors != null && !fieldErrors.isEmpty()) {
                    applyFieldErrors(fieldErrors);
                } else {
                    showToast(message);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                onError(message, statusCode, null);
            }
        });
    }

    private void applyFieldErrors(Map<String, String> fieldErrors) {
        if (fieldErrors.containsKey("email")) {
            binding.tilEmail.setError(fieldErrors.get("email"));
        }
        if (fieldErrors.containsKey("businessName")) {
            binding.tilBusinessName.setError(fieldErrors.get("businessName"));
        }
        if (fieldErrors.containsKey("fullName")) {
            binding.tilFullName.setError(fieldErrors.get("fullName"));
        }
        if (fieldErrors.containsKey("category")) {
            binding.tilCategory.setError(fieldErrors.get("category"));
        }
        if (fieldErrors.containsKey("password")) {
            binding.tilPassword.setError(fieldErrors.get("password"));
        }
    }

    private void clearErrors() {
        binding.tilBusinessName.setError(null);
        binding.tilFullName.setError(null);
        binding.tilCategory.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilAddress.setError(null);
        binding.tilTradeLicense.setError(null);
        binding.tilNationalId.setError(null);
        binding.tilCommissionRate.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    private void setLoading(boolean loading) {
        binding.btnRegister.setEnabled(!loading);
        binding.btnRegister.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etBusinessName.setEnabled(!loading);
        binding.etFullName.setEnabled(!loading);
        binding.actCategory.setEnabled(!loading);
        binding.etEmail.setEnabled(!loading);
        binding.etPhone.setEnabled(!loading);
        binding.etAddress.setEnabled(!loading);
        binding.etTradeLicense.setEnabled(!loading);
        binding.etNationalId.setEnabled(!loading);
        binding.etCommissionRate.setEnabled(!loading);
        binding.etPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
    }
}
