package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.GuideAvailability;
import com.example.horizontravel.data.model.GuideRating;
import com.example.horizontravel.databinding.ActivityGuideDetailBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Formatters;

import java.util.List;
import java.util.Locale;

public class GuideDetailActivity extends BaseActivity {

    public static final String EXTRA_GUIDE_ID = "extra_guide_id";

    private ActivityGuideDetailBinding binding;
    private long guideId;
    private Guide currentGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        guideId = getIntent().getLongExtra(EXTRA_GUIDE_ID, -1);
        if (guideId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_tour_guide, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnRequestGuide.setOnClickListener(v -> {
            if (currentGuide != null) {
                Intent intent = new Intent(this, RequestGuideActivity.class);
                intent.putExtra(RequestGuideActivity.EXTRA_GUIDE_ID, currentGuide.getId());
                intent.putExtra(RequestGuideActivity.EXTRA_GUIDE_NAME, currentGuide.getFullName());
                intent.putExtra(RequestGuideActivity.EXTRA_DEFAULT_PRICE,
                        currentGuide.getDefaultPrice() != null ? currentGuide.getDefaultPrice() : 0.0);
                intent.putExtra(RequestGuideActivity.EXTRA_NEGOTIABLE,
                        Boolean.TRUE.equals(currentGuide.getNegotiable()));
                startActivity(intent);
            }
        });

        loadGuideProfile();
        loadAvailability();
        loadRatings();
    }

    private void loadGuideProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getGuideById(guideId).enqueue(new ApiCallback<Guide>() {
            @Override
            public void onSuccess(Guide guide) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                currentGuide = guide;
                displayGuideInfo(guide);
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GuideDetailActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayGuideInfo(Guide guide) {
        if (guide == null) return;

        binding.tvGuideName.setText(guide.getFullName() != null ? guide.getFullName() : "Tour Guide");

        boolean available = Boolean.TRUE.equals(guide.getAvailable());
        binding.tvAvailabilityBadge.setText(available ? "AVAILABLE" : "UNAVAILABLE");
        binding.tvAvailabilityBadge.setTextColor(getResources().getColor(R.color.badge_text));

        String loc = guide.getLocation() != null && !guide.getLocation().isEmpty()
                ? guide.getLocation() : "Location not specified";
        binding.tvLocation.setText(loc);

        if (guide.getRatingAvg() != null && guide.getRatingCount() != null && guide.getRatingCount() > 0) {
            binding.layoutGuideRating.setVisibility(View.VISIBLE);
            binding.tvRating.setText(String.format(Locale.US, "%.1f (%d reviews)",
                    guide.getRatingAvg(), guide.getRatingCount()));
        } else {
            binding.layoutGuideRating.setVisibility(View.GONE);
        }

        int exp = guide.getExperienceYears() != null ? guide.getExperienceYears() : 0;
        binding.tvExperience.setText(getString(R.string.guide_experience_years, exp));

        binding.tvBio.setText(guide.getBio() != null && !guide.getBio().isEmpty()
                ? guide.getBio() : "No bio provided.");

        if (guide.getLanguages() != null && !guide.getLanguages().isEmpty()) {
            binding.tvLanguages.setText(String.join(", ", guide.getLanguages()));
        } else {
            binding.tvLanguages.setText(R.string.text_not_specified);
        }

        double price = guide.getDefaultPrice() != null ? guide.getDefaultPrice() : 0.0;
        binding.tvPricePerDay.setText(getString(R.string.guide_rate_per_day, Formatters.formatCurrency(price)));

        boolean negotiable = Boolean.TRUE.equals(guide.getNegotiable());
        binding.tvNegotiable.setVisibility(negotiable ? View.VISIBLE : View.GONE);
    }

    private void loadAvailability() {
        ApiClient.getApiService().getGuideAvailabilityByGuideId(guideId).enqueue(new ApiCallback<List<GuideAvailability>>() {
            @Override
            public void onSuccess(List<GuideAvailability> list) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutAvailabilityContainer.removeAllViews();

                if (list == null || list.isEmpty()) {
                    binding.tvNoAvailability.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoAvailability.setVisibility(View.GONE);
                    LayoutInflater inflater = LayoutInflater.from(GuideDetailActivity.this);
                    for (GuideAvailability a : list) {
                        com.example.horizontravel.databinding.ItemTwoLineBinding itemBinding =
                                com.example.horizontravel.databinding.ItemTwoLineBinding.inflate(inflater, binding.layoutAvailabilityContainer, false);

                        itemBinding.tvText1.setText(String.format(java.util.Locale.US, "%s  (%s - %s)",
                                a.getDate() != null ? Formatters.formatDate(a.getDate()) : "",
                                a.getStartTime() != null ? a.getStartTime() : "",
                                a.getEndTime() != null ? a.getEndTime() : ""));
                        itemBinding.tvText1.setTextColor(getResources().getColor(R.color.navy_primary));

                        String notes = a.getNotes() != null ? a.getNotes() : "";
                        String loc = a.getLocation() != null ? "Location: " + a.getLocation() : "";
                        itemBinding.tvText2.setText(String.format(java.util.Locale.US, "Status: %s  |  %s %s",
                                a.getStatus() != null ? a.getStatus() : "OPEN",
                                loc, notes).trim());
                        itemBinding.tvText2.setTextColor(getResources().getColor(R.color.on_surface_variant));

                        binding.layoutAvailabilityContainer.addView(itemBinding.getRoot());
                    }
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.tvNoAvailability.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadRatings() {
        ApiClient.getApiService().getRatingsForGuide(guideId).enqueue(new ApiCallback<List<GuideRating>>() {
            @Override
            public void onSuccess(List<GuideRating> list) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutRatingsContainer.removeAllViews();

                if (list == null || list.isEmpty()) {
                    binding.tvNoRatings.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoRatings.setVisibility(View.GONE);
                    LayoutInflater inflater = LayoutInflater.from(GuideDetailActivity.this);
                    for (GuideRating r : list) {
                        com.example.horizontravel.databinding.ItemTwoLineBinding itemBinding =
                                com.example.horizontravel.databinding.ItemTwoLineBinding.inflate(inflater, binding.layoutRatingsContainer, false);

                        itemBinding.tvText1.setText(String.format(Locale.US, "★ %d/5  •  %s",
                                r.getScore(),
                                r.getRatedDate() != null ? Formatters.formatDate(r.getRatedDate()) : ""));
                        itemBinding.tvText1.setTextColor(getResources().getColor(R.color.navy_primary));

                        String comment = (r.getComment() != null && !r.getComment().trim().isEmpty())
                                ? r.getComment() : "No comment provided.";
                        itemBinding.tvText2.setText(comment);
                        itemBinding.tvText2.setTextColor(getResources().getColor(R.color.on_surface_variant));

                        binding.layoutRatingsContainer.addView(itemBinding.getRoot());
                    }
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.tvNoRatings.setVisibility(View.VISIBLE);
            }
        });
    }
}
