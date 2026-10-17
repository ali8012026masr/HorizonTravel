package com.example.horizontravel.ui.shared;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.ChangePasswordRequest;
import com.example.horizontravel.data.model.MessageResponse;
import com.example.horizontravel.databinding.ActivityChangePasswordBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.util.SessionManager;
import com.example.horizontravel.util.Validators;

public class ChangePasswordActivity extends BaseActivity {
    private ActivityChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnChangePassword.setOnClickListener(v -> {
            if (isSingleClick()) {
                performChangePassword();
            }
        });
    }

    private void performChangePassword() {
        binding.tilCurrentPassword.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        String currentPassword = binding.etCurrentPassword.getText() != null ? binding.etCurrentPassword.getText().toString() : "";
        String newPassword = binding.etNewPassword.getText() != null ? binding.etNewPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";

        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.setError(getString(R.string.error_field_required));
            binding.etCurrentPassword.requestFocus();
            return;
        }

        if (!Validators.isValidPassword(newPassword)) {
            binding.tilNewPassword.setError(getString(R.string.error_password_too_short));
            binding.etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_do_not_match));
            binding.etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);
        ApiClient.getApiService().changePassword(request).enqueue(new ApiCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse response) {
                if (!isSafe()) return;
                setLoading(false);
                SessionManager.getInstance().clear();
                showToast(getString(R.string.password_changed_success_toast));

                Intent loginIntent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                setLoading(false);
                showToast(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnChangePassword.setEnabled(!loading);
        binding.btnChangePassword.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etCurrentPassword.setEnabled(!loading);
        binding.etNewPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
    }
}
