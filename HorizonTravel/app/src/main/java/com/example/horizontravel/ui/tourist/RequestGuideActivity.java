package com.example.horizontravel.ui.tourist;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.GuideBooking;
import com.example.horizontravel.data.model.GuideBookingRequest;
import com.example.horizontravel.databinding.ActivityRequestGuideBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RequestGuideActivity extends BaseActivity {

    public static final String EXTRA_GUIDE_ID = "extra_guide_id";
    public static final String EXTRA_GUIDE_NAME = "extra_guide_name";
    public static final String EXTRA_DEFAULT_PRICE = "extra_default_price";
    public static final String EXTRA_NEGOTIABLE = "extra_negotiable";

    private ActivityRequestGuideBinding binding;
    private long guideId;
    private boolean isNegotiable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestGuideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        guideId = getIntent().getLongExtra(EXTRA_GUIDE_ID, -1);
        String guideName = getIntent().getStringExtra(EXTRA_GUIDE_NAME);
        double defaultPrice = getIntent().getDoubleExtra(EXTRA_DEFAULT_PRICE, 0.0);
        isNegotiable = getIntent().getBooleanExtra(EXTRA_NEGOTIABLE, false);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (guideName != null) {
            binding.tvGuideHeader.setText(String.format(java.util.Locale.US, "Tour Guide: %s", guideName));
        }

        if (defaultPrice > 0) {
            binding.etPrice.setText(String.format(Locale.US, "%.2f", defaultPrice));
        }

        if (isNegotiable) {
            binding.tvNegotiableNotice.setVisibility(View.VISIBLE);
        }

        // Default date to tomorrow
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date tomorrow = new Date(System.currentTimeMillis() + 86400000L);
        binding.etDate.setText(sdf.format(tomorrow));

        binding.btnSubmitRequest.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String dateStr = binding.etDate.getText() != null ? binding.etDate.getText().toString().trim() : "";
        if (dateStr.isEmpty()) {
            binding.tilDate.setError(getString(R.string.error_field_required));
            return;
        }
        binding.tilDate.setError(null);

        String priceStr = binding.etPrice.getText() != null ? binding.etPrice.getText().toString().trim() : "";
        double price = 0.0;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                binding.tilPrice.setError(getString(R.string.error_invalid_price));
                return;
            }
        } catch (NumberFormatException e) {
            binding.tilPrice.setError(getString(R.string.error_invalid_price));
            return;
        }
        binding.tilPrice.setError(null);

        long touristId = SessionManager.getInstance().getUserId();
        GuideBookingRequest request = new GuideBookingRequest(
                touristId,
                guideId,
                null,
                dateStr,
                price,
                isNegotiable
        );

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitRequest.setEnabled(false);

        ApiClient.getApiService().createGuideBooking(request).enqueue(new ApiCallback<GuideBooking>() {
            @Override
            public void onSuccess(GuideBooking guideBooking) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(RequestGuideActivity.this, R.string.guide_request_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmitRequest.setEnabled(true);
                Toast.makeText(RequestGuideActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
