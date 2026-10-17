package com.example.horizontravel.ui.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.Provider;
import com.example.horizontravel.databinding.ActivityVerificationDetailBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Formatters;
import com.google.gson.Gson;

import java.util.Locale;

public class VerificationDetailActivity extends BaseActivity {
    public static final String EXTRA_ROLE = "verification_role";
    public static final String EXTRA_JSON = "verification_json";

    private ActivityVerificationDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerificationDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String role = getIntent().getStringExtra(EXTRA_ROLE);
        String json = getIntent().getStringExtra(EXTRA_JSON);
        if ("guide".equals(role)) {
            showGuide(new Gson().fromJson(json, Guide.class));
        } else if ("provider".equals(role)) {
            showProvider(new Gson().fromJson(json, Provider.class));
        } else {
            finish();
        }
    }

    private void showGuide(Guide guide) {
        if (guide == null) {
            finish();
            return;
        }
        binding.tvName.setText(value(guide.getFullName(), "Guide"));
        binding.tvRole.setText(getString(R.string.admin_verification_filter_guides));
        binding.tvContact.setText(lines(
                getString(R.string.admin_verification_email, value(guide.getEmail())),
                getString(R.string.admin_verification_phone, value(guide.getPhone())),
                getString(R.string.admin_verification_address, value(guide.getAddress()))));
        binding.tvProfile.setText(lines(
                getString(R.string.admin_verification_location, value(guide.getLocation())),
                guide.getExperienceYears() == null ? getString(R.string.admin_verification_not_available)
                        : getString(R.string.admin_verification_experience, guide.getExperienceYears()),
                getString(R.string.admin_verification_languages,
                        guide.getLanguages() == null || guide.getLanguages().isEmpty()
                                ? value(null) : String.join(", ", guide.getLanguages())),
                getString(R.string.admin_verification_bio, value(guide.getBio())),
                getString(R.string.admin_verification_price,
                        guide.getDefaultPrice() == null ? value(null)
                                : String.format(Locale.US, "%.2f", guide.getDefaultPrice())),
                getString(R.string.admin_verification_availability,
                        Boolean.TRUE.equals(guide.getAvailable()) ? "Yes" : "No"),
                getString(R.string.admin_verification_negotiable,
                        Boolean.TRUE.equals(guide.getNegotiable()) ? "Yes" : "No"),
                getString(R.string.admin_verification_rating,
                        guide.getRatingAvg() == null ? value(null) : String.format(Locale.US, "%.1f", guide.getRatingAvg()),
                        guide.getRatingCount() == null ? 0 : guide.getRatingCount())));
        binding.tvBusiness.setText(getString(R.string.admin_verification_not_available));
        binding.tvIdentity.setText(identity(guide.getNationalId(), guide.getStatus(),
                guide.getVerificationStatus(), guide.getRejectionReason()));
    }

    private void showProvider(Provider provider) {
        if (provider == null) {
            finish();
            return;
        }
        binding.tvName.setText(value(provider.getBusinessName(), provider.getFullName()));
        binding.tvRole.setText(getString(R.string.admin_verification_filter_providers));
        binding.tvContact.setText(lines(
                getString(R.string.admin_verification_email, value(provider.getEmail())),
                getString(R.string.admin_verification_phone, value(provider.getPhone())),
                getString(R.string.admin_verification_address, value(provider.getAddress()))));
        binding.tvProfile.setText(lines(
                getString(R.string.admin_verification_category, Formatters.capitalizeEnum(value(provider.getCategory()))),
                getString(R.string.admin_verification_rating,
                        provider.getRatingAvg() == null ? value(null) : String.format(Locale.US, "%.1f", provider.getRatingAvg()),
                        provider.getRatingCount() == null ? 0 : provider.getRatingCount())));
        binding.tvBusiness.setText(lines(
                getString(R.string.admin_verification_license, value(provider.getTradeLicenseNo())),
                getString(R.string.admin_verification_commission,
                        provider.getCommissionRate() == null ? value(null)
                                : String.format(Locale.US, "%.2f%%", provider.getCommissionRate()))));
        binding.tvIdentity.setText(identity(null, provider.getStatus(),
                provider.getVerificationStatus(), provider.getRejectionReason()));
    }

    private String identity(String nationalId, String status, String verification, String rejection) {
        String result = getString(R.string.admin_verification_national_id, value(nationalId))
                + "\n" + getString(R.string.admin_verification_status, value(status))
                + "\n" + getString(R.string.admin_verification_review_status, value(verification));
        if (rejection != null && !rejection.trim().isEmpty()) {
            result += "\n" + getString(R.string.admin_verification_rejection_reason, rejection);
        }
        return result;
    }

    private String lines(String... values) {
        return String.join("\n", values);
    }

    private String value(String value) {
        return value == null || value.trim().isEmpty()
                ? getString(R.string.admin_verification_not_available) : value;
    }

    private String value(String first, String fallback) {
        return first == null || first.trim().isEmpty() ? value(fallback) : first;
    }
}
