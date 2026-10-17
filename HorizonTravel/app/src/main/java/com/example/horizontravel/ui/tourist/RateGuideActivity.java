package com.example.horizontravel.ui.tourist;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.GuideRating;
import com.example.horizontravel.data.model.GuideRatingRequest;
import com.example.horizontravel.databinding.ActivityRateGuideBinding;
import com.example.horizontravel.ui.BaseActivity;

public class RateGuideActivity extends BaseActivity {

    public static final String EXTRA_GUIDE_BOOKING_ID = "extra_guide_booking_id";

    private ActivityRateGuideBinding binding;
    private long guideBookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRateGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        guideBookingId = getIntent().getLongExtra(EXTRA_GUIDE_BOOKING_ID, -1);
        if (guideBookingId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_guide_booking, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSubmitRating.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        int score = (int) binding.ratingBar.getRating();
        if (score < 1) score = 1;
        if (score > 5) score = 5;

        String comment = null;
        if (binding.etComment.getText() != null) {
            comment = binding.etComment.getText().toString().trim();
        }

        GuideRatingRequest request = new GuideRatingRequest(
                guideBookingId,
                score,
                comment
        );

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitRating.setEnabled(false);

        ApiClient.getApiService().submitGuideRating(request).enqueue(new ApiCallback<GuideRating>() {
            @Override
            public void onSuccess(GuideRating rating) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(RateGuideActivity.this, R.string.rating_submitted_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmitRating.setEnabled(true);
                Toast.makeText(RateGuideActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
