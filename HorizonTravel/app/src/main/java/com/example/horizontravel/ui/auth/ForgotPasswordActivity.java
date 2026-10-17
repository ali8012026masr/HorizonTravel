package com.example.horizontravel.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.ForgotPasswordRequest;
import com.example.horizontravel.data.model.MessageResponse;
import com.example.horizontravel.databinding.ActivityForgotPasswordBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Validators;

public class ForgotPasswordActivity extends BaseActivity {
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSubmit.setOnClickListener(v -> {
            if (isSingleClick()) {
                submitForgotPassword();
            }
        });

        binding.tvAlreadyHaveToken.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, ResetPasswordActivity.class));
            }
        });
    }

    private void submitForgotPassword() {
        binding.tilEmail.setError(null);
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";

        if (!Validators.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            binding.etEmail.requestFocus();
            return;
        }

        setLoading(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        ApiClient.getApiService().forgotPassword(request).enqueue(new ApiCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse response) {
                if (!isSafe()) return;
                setLoading(false);
                String msg = response != null && response.getMessage() != null
                        ? response.getMessage()
                        : getString(R.string.reset_token_sent_toast);
                showToast(msg);

                Intent resetIntent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                startActivity(resetIntent);
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
        binding.btnSubmit.setEnabled(!loading);
        binding.btnSubmit.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        binding.pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.etEmail.setEnabled(!loading);
    }
}
