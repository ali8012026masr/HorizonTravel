package com.example.horizontravel.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.LoginRequest;
import com.example.horizontravel.data.model.LoginResponse;
import com.example.horizontravel.databinding.ActivityLoginBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.RoleRouter;
import com.example.horizontravel.util.SessionManager;
import com.example.horizontravel.util.Validators;

public class LoginActivity extends BaseActivity {
    public static final String EXTRA_EMAIL = "extra_email";
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String initialEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        if (initialEmail != null && !initialEmail.isEmpty()) {
            binding.etEmail.setText(initialEmail);
            binding.etPassword.requestFocus();
        }

        binding.btnLogin.setOnClickListener(v -> {
            if (isSingleClick()) {
                performLogin();
            }
        });

        binding.tvRegister.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, RegisterChooserActivity.class));
            }
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, ForgotPasswordActivity.class));
            }
        });

        binding.btnBack.setOnClickListener(v -> navigateBack());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBack();
            }
        });

        binding.tvExploreGuest.setOnClickListener(v -> {
            Intent intent = new Intent(this, com.example.horizontravel.MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void navigateBack() {
        if (!isTaskRoot()) {
            finish();
            return;
        }

        Intent intent = new Intent(this, com.example.horizontravel.MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void performLogin() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        if (!Validators.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            binding.etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_field_required));
            binding.etPassword.requestFocus();
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);
        ApiClient.getApiService().login(request).enqueue(new ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                if (!isSafe()) return;
                setLoading(false);

                if (response == null || response.getRole() == null) {
                    showToast(getString(R.string.error_generic));
                    return;
                }

                SessionManager.getInstance().save(response);

                String role = response.getRole();
                String status = response.getStatus();
                String verificationStatus = response.getVerificationStatus();

                boolean isPending = false;
                if (status != null && !Constants.UserStatus.ACTIVE.equalsIgnoreCase(status)) {
                    isPending = true;
                } else if (Constants.Role.TOUR_GUIDE.equalsIgnoreCase(role) || Constants.Role.SERVICE_PROVIDER.equalsIgnoreCase(role)) {
                    if (verificationStatus != null && !Constants.VerificationStatus.APPROVED.equalsIgnoreCase(verificationStatus)) {
                        isPending = true;
                    }
                }

                Intent intent;
                if (isPending) {
                    intent = new Intent(LoginActivity.this, PendingVerificationActivity.class);
                } else {
                    intent = RoleRouter.shellIntentFor(LoginActivity.this, role);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                setLoading(false);

                if (statusCode == 401) {
                    binding.tilPassword.setError(getString(R.string.error_bad_credentials));
                } else if (statusCode == 423) {
                    showToast(getString(R.string.error_account_locked));
                } else {
                    showToast(message);
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnLogin.setEnabled(!loading);
        binding.btnLogin.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etEmail.setEnabled(!loading);
        binding.etPassword.setEnabled(!loading);
    }
}
