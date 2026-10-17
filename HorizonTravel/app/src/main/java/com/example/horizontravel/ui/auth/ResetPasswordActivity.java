package com.example.horizontravel.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.MessageResponse;
import com.example.horizontravel.data.model.ResetPasswordRequest;
import com.example.horizontravel.databinding.ActivityResetPasswordBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Validators;

public class ResetPasswordActivity extends BaseActivity {
    private ActivityResetPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnResetPassword.setOnClickListener(v -> {
            if (isSingleClick()) {
                performResetPassword();
            }
        });
    }

    private void performResetPassword() {
        binding.tilToken.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        String token = binding.etToken.getText() != null ? binding.etToken.getText().toString().trim() : "";
        String newPassword = binding.etNewPassword.getText() != null ? binding.etNewPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";

        if (!Validators.isNotEmpty(token)) {
            binding.tilToken.setError(getString(R.string.error_field_required));
            binding.etToken.requestFocus();
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

        ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);
        ApiClient.getApiService().resetPassword(request).enqueue(new ApiCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse response) {
                if (!isSafe()) return;
                setLoading(false);
                showToast(getString(R.string.password_reset_success_toast));

                Intent loginIntent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        binding.btnResetPassword.setEnabled(!loading);
        binding.btnResetPassword.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etToken.setEnabled(!loading);
        binding.etNewPassword.setEnabled(!loading);
        binding.etConfirmPassword.setEnabled(!loading);
    }
}
