package com.example.horizontravel.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import com.example.horizontravel.databinding.ActivityRegisterChooserBinding;
import com.example.horizontravel.ui.BaseActivity;

public class RegisterChooserActivity extends BaseActivity {
    private ActivityRegisterChooserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterChooserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cardTourist.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, RegisterTouristActivity.class));
            }
        });

        binding.cardGuide.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, RegisterGuideActivity.class));
            }
        });

        binding.cardProvider.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, RegisterProviderActivity.class));
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
}
