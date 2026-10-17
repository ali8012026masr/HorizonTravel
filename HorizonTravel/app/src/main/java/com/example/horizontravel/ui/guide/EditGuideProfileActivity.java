package com.example.horizontravel.ui.guide;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.UpdateGuideRequest;
import com.example.horizontravel.databinding.ActivityEditGuideProfileBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EditGuideProfileActivity extends BaseActivity {

    private ActivityEditGuideProfileBinding binding;
    private long guideId;
    private Guide currentGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditGuideProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.TOUR_GUIDE.equalsIgnoreCase(session.getRole())) {
            finish();
            return;
        }

        guideId = session.getUserId();
        if (guideId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_session, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> saveProfile());
        loadProfile();
    }

    private void loadProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getGuideById(guideId).enqueue(new ApiCallback<Guide>() {
            @Override
            public void onSuccess(Guide guide) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                currentGuide = guide;

                if (guide != null) {
                    if (guide.getFullName() != null) {
                        binding.etFullName.setText(guide.getFullName());
                    }
                    if (guide.getPhone() != null) {
                        binding.etPhone.setText(guide.getPhone());
                    }
                    if (guide.getAddress() != null) {
                        binding.etAddress.setText(guide.getAddress());
                    }
                    if (guide.getLocation() != null) {
                        binding.etLocation.setText(guide.getLocation());
                    }
                    if (guide.getLanguages() != null && !guide.getLanguages().isEmpty()) {
                        binding.etLanguages.setText(String.join(", ", guide.getLanguages()));
                    }
                    if (guide.getExperienceYears() != null) {
                        binding.etExperience.setText(String.valueOf(guide.getExperienceYears()));
                    }
                    if (guide.getDefaultPrice() != null) {
                        binding.etPrice.setText(String.format(Locale.US, "%.2f", guide.getDefaultPrice()));
                    }
                    binding.switchNegotiable.setChecked(Boolean.TRUE.equals(guide.getNegotiable()));
                    if (guide.getBio() != null) {
                        binding.etBio.setText(guide.getBio());
                    }
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditGuideProfileActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
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
        String location = binding.etLocation.getText() != null ? binding.etLocation.getText().toString().trim() : null;
        String bio = binding.etBio.getText() != null ? binding.etBio.getText().toString().trim() : null;

        String langStr = binding.etLanguages.getText() != null ? binding.etLanguages.getText().toString().trim() : "";
        List<String> languages = new ArrayList<>();
        if (!langStr.isEmpty()) {
            String[] parts = langStr.split(",");
            for (String p : parts) {
                String clean = p.trim();
                if (!clean.isEmpty()) {
                    languages.add(clean);
                }
            }
        }

        String expStr = binding.etExperience.getText() != null ? binding.etExperience.getText().toString().trim() : "";
        Integer experienceYears = null;
        if (!expStr.isEmpty()) {
            try {
                experienceYears = Integer.parseInt(expStr);
            } catch (NumberFormatException ignored) {}
        }

        String priceStr = binding.etPrice.getText() != null ? binding.etPrice.getText().toString().trim() : "";
        Double defaultPrice = null;
        if (!priceStr.isEmpty()) {
            try {
                defaultPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException ignored) {}
        }

        boolean negotiable = binding.switchNegotiable.isChecked();

        UpdateGuideRequest request = new UpdateGuideRequest();
        request.setFullName(fullName);
        request.setPhone(phone);
        request.setAddress(address);
        request.setLocation(location);
        request.setBio(bio);
        request.setLanguages(languages);
        request.setExperienceYears(experienceYears);
        request.setDefaultPrice(defaultPrice);
        request.setNegotiable(negotiable);
        if (currentGuide != null) {
            request.setAvailable(currentGuide.getAvailable());
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        ApiClient.getApiService().updateGuideProfile(guideId, request).enqueue(new ApiCallback<Guide>() {
            @Override
            public void onSuccess(Guide guide) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditGuideProfileActivity.this, R.string.profile_updated_success, Toast.LENGTH_SHORT).show();
                if (guide != null && guide.getFullName() != null) {
                    SessionManager.getInstance().setFullName(guide.getFullName());
                }
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                Toast.makeText(EditGuideProfileActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
