package com.example.horizontravel.ui.guide;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.GuideRating;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.UpdateGuideRequest;
import com.example.horizontravel.databinding.FragmentGuideProfileBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.shared.ChangePasswordActivity;
import com.example.horizontravel.ui.shared.NotificationsActivity;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuideProfileFragment extends BaseFragment<FragmentGuideProfileBinding> {

    private GuideRatingAdapter ratingAdapter;
    private long guideId;
    private Guide currentGuide;
    private boolean isUpdatingAvailability = false;

    @Override
    protected FragmentGuideProfileBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGuideProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        guideId = SessionManager.getInstance().getUserId();

        setupInitialSessionData();
        setupRecyclerView();
        setupListeners();
        loadProfile();
        loadRatings();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
        loadRatings();
    }

    private void setupInitialSessionData() {
        binding.tvGuideName.setText(SessionManager.getInstance().getFullName());
        binding.tvGuideEmail.setText(SessionManager.getInstance().getEmail());
    }

    private void setupRecyclerView() {
        ratingAdapter = new GuideRatingAdapter();
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReviews.setAdapter(ratingAdapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadProfile();
            loadRatings();
        });

        binding.switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingAvailability && currentGuide != null) {
                toggleAvailability(isChecked);
            }
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), EditGuideProfileActivity.class));
        });

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NotificationsActivity.class));
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadProfile() {
        if (!isSafe() || guideId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getGuideById(guideId).enqueue(new ApiCallback<Guide>() {
            @Override
            public void onSuccess(Guide guide) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                currentGuide = guide;
                displayGuide(guide);
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void displayGuide(Guide guide) {
        if (guide == null) return;

        if (guide.getFullName() != null) {
            binding.tvGuideName.setText(guide.getFullName());
            SessionManager.getInstance().setFullName(guide.getFullName());
        }
        if (guide.getEmail() != null) {
            binding.tvGuideEmail.setText(guide.getEmail());
        }

        String status = guide.getStatus() != null ? guide.getStatus() : "ACTIVE";
        binding.tvStatusBadge.setText(status);
        binding.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(
                requireContext().getColor(Constants.UserStatus.ACTIVE.equalsIgnoreCase(status)
                        ? R.color.badge_success : R.color.badge_pending)));
        binding.tvStatusBadge.setTextColor(requireContext().getColor(R.color.badge_text));

        isUpdatingAvailability = true;
        binding.switchAvailability.setChecked(Boolean.TRUE.equals(guide.getAvailable()));
        isUpdatingAvailability = false;

        String loc = guide.getLocation() != null && !guide.getLocation().isEmpty()
                ? "Location: " + guide.getLocation() : "Location: Not specified";
        binding.tvLocation.setText(loc);

        if (guide.getLanguages() != null && !guide.getLanguages().isEmpty()) {
            binding.tvLanguages.setText(String.format(java.util.Locale.US, "Languages: %s", String.join(", ", guide.getLanguages())));
        } else {
            binding.tvLanguages.setText(R.string.text_languages__none_specified);
        }

        int exp = guide.getExperienceYears() != null ? guide.getExperienceYears() : 0;
        binding.tvExperience.setText(getString(R.string.guide_experience_label, exp));

        double price = guide.getDefaultPrice() != null ? guide.getDefaultPrice() : 0.0;
        binding.tvDefaultPrice.setText(getString(R.string.guide_default_price_label, Formatters.formatCurrency(price)));

        if (guide.getPhone() != null && !guide.getPhone().isEmpty()) {
            binding.tvPhone.setText(String.format(java.util.Locale.US, "Phone: %s", guide.getPhone()));
            binding.tvPhone.setVisibility(View.VISIBLE);
        } else {
            binding.tvPhone.setVisibility(View.GONE);
        }

        if (guide.getAddress() != null && !guide.getAddress().isEmpty()) {
            binding.tvAddress.setText(String.format(java.util.Locale.US, "Address: %s", guide.getAddress()));
            binding.tvAddress.setVisibility(View.VISIBLE);
        } else {
            binding.tvAddress.setVisibility(View.GONE);
        }

        if (guide.getBio() != null && !guide.getBio().isEmpty()) {
            binding.tvBio.setText(String.format(java.util.Locale.US, "Bio:\n%s", guide.getBio()));
            binding.tvBio.setVisibility(View.VISIBLE);
        } else {
            binding.tvBio.setVisibility(View.GONE);
        }
    }

    private void toggleAvailability(boolean available) {
        if (!isSafe() || guideId <= 0 || currentGuide == null) return;

        UpdateGuideRequest request = new UpdateGuideRequest();
        request.setFullName(currentGuide.getFullName());
        request.setPhone(currentGuide.getPhone());
        request.setAddress(currentGuide.getAddress());
        request.setLocation(currentGuide.getLocation());
        request.setBio(currentGuide.getBio());
        request.setLanguages(currentGuide.getLanguages());
        request.setExperienceYears(currentGuide.getExperienceYears());
        request.setDefaultPrice(currentGuide.getDefaultPrice());
        request.setNegotiable(currentGuide.getNegotiable());
        request.setAvailable(available);

        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().updateGuideProfile(guideId, request).enqueue(new ApiCallback<Guide>() {
            @Override
            public void onSuccess(Guide guide) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                currentGuide = guide;
                Toast.makeText(requireContext(),
                        available ? "Status set to Available" : "Status set to Unavailable",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                isUpdatingAvailability = true;
                binding.switchAvailability.setChecked(!available);
                isUpdatingAvailability = false;
                Toast.makeText(requireContext(), message != null ? message : "Unable to update availability", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRatings() {
        if (!isSafe() || guideId <= 0) return;

        ApiClient.getApiService().getRatingsForGuide(guideId).enqueue(new ApiCallback<List<GuideRating>>() {
            @Override
            public void onSuccess(List<GuideRating> list) {
                if (!isSafe()) return;

                if (list == null || list.isEmpty()) {
                    ratingAdapter.setRatings(new ArrayList<>());
                    binding.tvNoReviews.setVisibility(View.VISIBLE);
                    binding.tvRatingsHeader.setText(getString(R.string.guide_reviews_title));
                } else {
                    ratingAdapter.setRatings(list);
                    binding.tvNoReviews.setVisibility(View.GONE);

                    double sum = 0.0;
                    for (GuideRating r : list) {
                        sum += r.getScore();
                    }
                    double avg = sum / list.size();
                    binding.tvRatingsHeader.setText(String.format(Locale.US,
                            "Reviews & Ratings (%.1f ★ • %d reviews)", avg, list.size()));
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.tvNoReviews.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLogoutConfirmation() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    SessionManager.getInstance().clear();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
