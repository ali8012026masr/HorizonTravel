package com.example.horizontravel.ui.tourist;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.ServiceRating;
import com.example.horizontravel.data.model.ServiceRatingRequest;
import com.example.horizontravel.databinding.ActivityRateServiceBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

public class RateServiceActivity extends BaseActivity {

    public static final String EXTRA_BOOKING_ID = "extra_booking_id";
    public static final String EXTRA_PROVIDER_ID = "extra_provider_id";

    private ActivityRateServiceBinding binding;
    private long bookingId;
    private long providerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRateServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1);
        providerId = getIntent().getLongExtra(EXTRA_PROVIDER_ID, 0);

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

        long touristId = SessionManager.getInstance().getUserId();
        ServiceRatingRequest request = new ServiceRatingRequest(
                bookingId > 0 ? bookingId : null,
                touristId,
                providerId > 0 ? providerId : null,
                score,
                comment
        );

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitRating.setEnabled(false);

        ApiClient.getApiService().submitServiceRating(request).enqueue(new ApiCallback<ServiceRating>() {
            @Override
            public void onSuccess(ServiceRating rating) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(RateServiceActivity.this, R.string.rating_submitted_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmitRating.setEnabled(true);
                Toast.makeText(RateServiceActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
