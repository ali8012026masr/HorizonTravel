package com.example.horizontravel.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.RegisterTouristRequest;
import com.example.horizontravel.data.model.Tourist;
import com.example.horizontravel.databinding.ActivityRegisterTouristBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Validators;

import java.util.Map;

public class RegisterTouristActivity extends BaseActivity {
    private ActivityRegisterTouristBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterTouristBinding.inflate(getLayoutInflater());
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

        RegisterTouristRequest request = new RegisterTouristRequest(
                fullName,
                email,
                password,
                phone.isEmpty() ? null : phone,
                address.isEmpty() ? null : address
        );

        ApiClient.getApiService().registerTourist(request).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist data) {
                if (!isSafe()) return;
                setLoading(false);
                showToast(getString(R.string.registration_success_tourist));

                Intent loginIntent = new Intent(RegisterTouristActivity.this, LoginActivity.class);
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
        if (fieldErrors.containsKey("phone")) {
            binding.tilPhone.setError(fieldErrors.get("phone"));
        }
        if (fieldErrors.containsKey("address")) {
            binding.tilAddress.setError(fieldErrors.get("address"));
        }
    }

    private void clearErrors() {
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilAddress.setError(null);
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
        binding.etPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
    }
}
