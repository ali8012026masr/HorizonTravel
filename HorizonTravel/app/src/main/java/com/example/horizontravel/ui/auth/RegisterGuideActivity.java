package com.example.horizontravel.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.RegisterGuideRequest;
import com.example.horizontravel.databinding.ActivityRegisterGuideBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegisterGuideActivity extends BaseActivity {
    private ActivityRegisterGuideBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : "";
        String nationalId = binding.etNationalId.getText() != null ? binding.etNationalId.getText().toString().trim() : "";
        String bio = binding.etBio.getText() != null ? binding.etBio.getText().toString().trim() : "";
        String languagesStr = binding.etLanguages.getText() != null ? binding.etLanguages.getText().toString().trim() : "";
        String location = binding.etLocation.getText() != null ? binding.etLocation.getText().toString().trim() : "";
        String expYearsStr = binding.etExperienceYears.getText() != null ? binding.etExperienceYears.getText().toString().trim() : "";
        String defaultPriceStr = binding.etDefaultPrice.getText() != null ? binding.etDefaultPrice.getText().toString().trim() : "";
        boolean isNegotiable = binding.switchNegotiable.isChecked();
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";

        if (!Validators.isNotEmpty(fullName)) {
            binding.tilFullName.setError(getString(R.string.error_field_required));
            binding.etFullName.requestFocus();
            return;
        }

        if (!Validators.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            binding.etEmail.requestFocus();
            return;
        }

        Integer experienceYears = null;
        if (!expYearsStr.isEmpty()) {
            try {
                int exp = Integer.parseInt(expYearsStr);
                if (exp < 0) {
                    binding.tilExperienceYears.setError(getString(R.string.error_invalid_experience));
                    binding.etExperienceYears.requestFocus();
                    return;
                }
                experienceYears = exp;
            } catch (NumberFormatException e) {
                binding.tilExperienceYears.setError(getString(R.string.error_invalid_experience));
                binding.etExperienceYears.requestFocus();
                return;
            }
        }

        Double defaultPrice = null;
        if (!defaultPriceStr.isEmpty()) {
            try {
                double price = Double.parseDouble(defaultPriceStr);
                if (price < 0) {
                    binding.tilDefaultPrice.setError(getString(R.string.error_invalid_price));
                    binding.etDefaultPrice.requestFocus();
                    return;
                }
                defaultPrice = price;
            } catch (NumberFormatException e) {
                binding.tilDefaultPrice.setError(getString(R.string.error_invalid_price));
                binding.etDefaultPrice.requestFocus();
                return;
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

        List<String> languageList = new ArrayList<>();
        if (!languagesStr.isEmpty()) {
            String[] splits = languagesStr.split(",");
            for (String split : splits) {
                String lang = split.trim();
                if (!lang.isEmpty()) {
                    languageList.add(lang);
                }
            }
        }

        setLoading(true);

        RegisterGuideRequest request = new RegisterGuideRequest();
        request.setFullName(fullName);
        request.setEmail(email);
        request.setPassword(password);
        request.setPhone(phone.isEmpty() ? null : phone);
        request.setAddress(address.isEmpty() ? null : address);
        request.setNationalId(nationalId.isEmpty() ? null : nationalId);
        request.setBio(bio.isEmpty() ? null : bio);
        request.setLanguages(languageList.isEmpty() ? null : languageList);
        request.setLocation(location.isEmpty() ? null : location);
        request.setExperienceYears(experienceYears);
        request.setDefaultPrice(defaultPrice);
        request.setNegotiable(isNegotiable);

        ApiClient.getApiService().registerGuide(request).enqueue(new ApiCallback<Guide>() {
            @Override
            public void onSuccess(Guide data) {
                if (!isSafe()) return;
                setLoading(false);
                showToast(getString(R.string.registration_success_awaiting_approval));

                Intent loginIntent = new Intent(RegisterGuideActivity.this, LoginActivity.class);
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
        if (fieldErrors.containsKey("fullName")) {
            binding.tilFullName.setError(fieldErrors.get("fullName"));
        }
        if (fieldErrors.containsKey("password")) {
            binding.tilPassword.setError(fieldErrors.get("password"));
        }
        if (fieldErrors.containsKey("experienceYears")) {
            binding.tilExperienceYears.setError(fieldErrors.get("experienceYears"));
        }
        if (fieldErrors.containsKey("defaultPrice")) {
            binding.tilDefaultPrice.setError(fieldErrors.get("defaultPrice"));
        }
    }

    private void clearErrors() {
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilAddress.setError(null);
        binding.tilNationalId.setError(null);
        binding.tilBio.setError(null);
        binding.tilLanguages.setError(null);
        binding.tilLocation.setError(null);
        binding.tilExperienceYears.setError(null);
        binding.tilDefaultPrice.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    private void setLoading(boolean loading) {
        binding.btnRegister.setEnabled(!loading);
        binding.btnRegister.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etFullName.setEnabled(!loading);
        binding.etEmail.setEnabled(!loading);
        binding.etPhone.setEnabled(!loading);
        binding.etAddress.setEnabled(!loading);
        binding.etNationalId.setEnabled(!loading);
        binding.etBio.setEnabled(!loading);
        binding.etLanguages.setEnabled(!loading);
        binding.etLocation.setEnabled(!loading);
        binding.etExperienceYears.setEnabled(!loading);
        binding.etDefaultPrice.setEnabled(!loading);
        binding.switchNegotiable.setEnabled(!loading);
        binding.etPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
    }
}
