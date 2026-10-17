package com.example.horizontravel.ui.tourist;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Tourist;
import com.example.horizontravel.data.model.UpdateTouristRequest;
import com.example.horizontravel.databinding.ActivityEditProfileBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

public class EditProfileActivity extends BaseActivity {

    private ActivityEditProfileBinding binding;
    private long touristId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        touristId = SessionManager.getInstance().getUserId();
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.etFullName.setText(SessionManager.getInstance().getFullName());

        loadCurrentDetails();
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getTouristProfile(touristId).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist tourist) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                if (tourist != null) {
                    if (tourist.getFullName() != null) {
                        binding.etFullName.setText(tourist.getFullName());
                    }
                    if (tourist.getPhone() != null) {
                        binding.etPhone.setText(tourist.getPhone());
                    }
                    if (tourist.getAddress() != null) {
                        binding.etAddress.setText(tourist.getAddress());
                    }
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void saveProfile() {
        String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
        if (fullName.isEmpty()) {
            binding.tilFullName.setError(getString(R.string.error_field_required));
            return;
        }
        binding.tilFullName.setError(null);

        String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : null;
        String address = binding.etAddress.getText() != null ? binding.etAddress.getText().toString().trim() : null;

        UpdateTouristRequest request = new UpdateTouristRequest(fullName, phone, address);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveProfile.setEnabled(false);

        ApiClient.getApiService().updateTouristProfile(touristId, request).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist tourist) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfileActivity.this, R.string.profile_updated_success, Toast.LENGTH_SHORT).show();
                if (tourist != null && tourist.getFullName() != null) {
                    SessionManager.getInstance().setFullName(tourist.getFullName());
                }
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSaveProfile.setEnabled(true);
                Toast.makeText(EditProfileActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
