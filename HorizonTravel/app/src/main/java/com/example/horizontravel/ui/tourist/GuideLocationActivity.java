package com.example.horizontravel.ui.tourist;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Location;
import com.example.horizontravel.databinding.ActivityGuideLocationBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Formatters;

import java.util.Locale;

public class GuideLocationActivity extends BaseActivity {

    public static final String EXTRA_GUIDE_ID = "extra_guide_id";

    private ActivityGuideLocationBinding binding;
    private long guideId;
    private Location currentLocation;

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadLocation();
            refreshHandler.postDelayed(this, 15000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        guideId = getIntent().getLongExtra(EXTRA_GUIDE_ID, -1);
        if (guideId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_tour_guide, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnOpenMaps.setOnClickListener(v -> {
            if (currentLocation != null) {
                openInMaps(currentLocation.getLatitude(), currentLocation.getLongitude());
            }
        });

        binding.btnCheckAgain.setOnClickListener(v -> loadLocation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onDestroy() {
        refreshHandler.removeCallbacks(refreshRunnable);
        super.onDestroy();
    }

    private void loadLocation() {
        ApiClient.getApiService().getGuideLocation(guideId).enqueue(new ApiCallback<Location>() {
            @Override
            public void onSuccess(Location loc) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);

                if (loc != null && (loc.getLatitude() != 0.0 || loc.getLongitude() != 0.0)) {
                    currentLocation = loc;
                    binding.cardLocationInfo.setVisibility(View.VISIBLE);
                    binding.layoutNotBroadcasted.setVisibility(View.GONE);

                    binding.tvCoordinates.setText(String.format(Locale.US,
                            "Coordinates: %.5f, %.5f", loc.getLatitude(), loc.getLongitude()));

                    String date = loc.getUpdatedAt() != null ? Formatters.formatDateTime(loc.getUpdatedAt()) : "Just now";
                    binding.tvLastUpdated.setText(String.format(java.util.Locale.US, "Last updated: %s", date));
                } else {
                    showNotBroadcasted();
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                showNotBroadcasted();
            }
        });
    }

    private void showNotBroadcasted() {
        binding.cardLocationInfo.setVisibility(View.GONE);
        binding.layoutNotBroadcasted.setVisibility(View.VISIBLE);
    }

    private void openInMaps(double lat, double lng) {
        String uriStr = String.format(Locale.US, "geo:%.5f,%.5f?q=%.5f,%.5f(Tour Guide)", lat, lng, lat, lng);
        Uri gmmIntentUri = Uri.parse(uriStr);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            // Fallback to web browser maps
            Uri webUri = Uri.parse(String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%.5f,%.5f", lat, lng));
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }
}
