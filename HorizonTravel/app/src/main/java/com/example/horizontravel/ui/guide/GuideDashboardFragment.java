package com.example.horizontravel.ui.guide;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.GuideBooking;
import com.example.horizontravel.data.model.UpdateLocationRequest;
import com.example.horizontravel.databinding.FragmentGuideDashboardBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuideDashboardFragment extends BaseFragment<FragmentGuideDashboardBinding> {

    private static final String PREF_NAME = "guide_gps_prefs";
    private static final String PREF_KEY_GPS_PREFIX = "pref_gps_broadcast_";
    private static final long BROADCAST_INTERVAL_MS = 30000L;

    private GuideIncomingBookingAdapter adapter;
    private long guideId;

    private final Handler broadcastHandler = new Handler(Looper.getMainLooper());
    private boolean isBroadcasting = false;

    private final Runnable broadcastRunnable = new Runnable() {
        @Override
        public void run() {
            broadcastCurrentLocation();
            if (isBroadcasting) {
                broadcastHandler.postDelayed(this, BROADCAST_INTERVAL_MS);
            }
        }
    };

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fine = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarse = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (fine || coarse) {
                    startGpsBroadcast();
                } else {
                    if (binding != null) {
                        binding.switchGpsBroadcast.setChecked(false);
                        binding.switchGpsBroadcast.setEnabled(false);
                        binding.tvGpsStatusDesc.setText(R.string.guide_gps_permission_denied);
                    }
                    saveGpsPref(false);
                    showPermissionDeniedDialog();
                }
            });

    @Override
    protected FragmentGuideDashboardBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGuideDashboardBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        guideId = SessionManager.getInstance().getUserId();

        setupRecyclerView();
        setupGpsSwitch();
        setupListeners();
        loadDashboardData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isGpsPrefEnabled() && hasLocationPermission()) {
            startGpsBroadcast();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopGpsBroadcast();
    }

    @Override
    public void onDestroyView() {
        stopGpsBroadcast();
        super.onDestroyView();
    }

    private void setupRecyclerView() {
        adapter = new GuideIncomingBookingAdapter(new GuideIncomingBookingAdapter.OnBookingActionListener() {
            @Override
            public void onAccept(GuideBooking booking) {}

            @Override
            public void onDecline(GuideBooking booking) {}

            @Override
            public void onMarkCompleted(GuideBooking booking) {}

            @Override
            public void onMarkPaymentReceived(GuideBooking booking) {}

            @Override
            public void onItemClick(GuideBooking booking) {}
        }, false);

        binding.rvUpcomingBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcomingBookings.setAdapter(adapter);
    }

    private void setupGpsSwitch() {
        boolean enabled = isGpsPrefEnabled();
        binding.switchGpsBroadcast.setChecked(enabled);
        binding.tvGpsStatusDesc.setText(enabled ? R.string.guide_gps_broadcast_desc : R.string.guide_gps_off_desc);

        binding.switchGpsBroadcast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (hasLocationPermission()) {
                    startGpsBroadcast();
                } else {
                    showLocationRationaleDialog();
                }
            } else {
                stopGpsBroadcast();
                saveGpsPref(false);
                binding.tvGpsStatusDesc.setText(R.string.guide_gps_off_desc);
            }
        });
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadDashboardData);
        binding.btnRetry.setOnClickListener(v -> loadDashboardData());
    }

    private void loadDashboardData() {
        if (!isSafe() || guideId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);

        // 1. Load Guide Profile for ratings and availability
        ApiClient.getApiService().getGuideById(guideId).enqueue(new ApiCallback<Guide>() {
            @Override
            public void onSuccess(Guide guide) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (guide != null) {
                    String name = guide.getFullName() != null ? guide.getFullName() : SessionManager.getInstance().getFullName();
                    binding.tvWelcomeName.setText(String.format(java.util.Locale.US, "Hello, %s", name));

                    double rating = guide.getRatingAvg() != null ? guide.getRatingAvg() : 0.0;
                    int count = guide.getRatingCount() != null ? guide.getRatingCount() : 0;
                    binding.tvAvgRating.setText(String.format(Locale.US, "%.1f", rating));
                    binding.tvReviewCount.setText(getString(R.string.guide_rating_count_format, count));

                    boolean available = Boolean.TRUE.equals(guide.getAvailable());
                    binding.tvAvailabilityBadge.setText(available ? "AVAILABLE FOR HIRE" : "UNAVAILABLE");
                    binding.tvAvailabilityBadge.setTextColor(getResources().getColor(
                            available ? R.color.teal_secondary : R.color.on_surface_variant));
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        // 2. Load Bookings for Upcoming Confirmed Bookings
        ApiClient.getApiService().getGuideBookingsForGuide(guideId).enqueue(new ApiCallback<List<GuideBooking>>() {
            @Override
            public void onSuccess(List<GuideBooking> list) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                List<GuideBooking> upcoming = new ArrayList<>();
                String today = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
                if (list != null) {
                    for (GuideBooking b : list) {
                        if (Constants.GuideBookingStatus.ACCEPTED.equalsIgnoreCase(b.getStatus())) {
                            if (b.getScheduleDate() != null && b.getScheduleDate().compareTo(today) >= 0) {
                                upcoming.add(b);
                            }
                        }
                    }
                }

                if (upcoming.isEmpty()) {
                    adapter.setBookings(new ArrayList<>());
                    binding.layoutEmptyUpcoming.setVisibility(View.VISIBLE);
                } else {
                    adapter.setBookings(upcoming);
                    binding.layoutEmptyUpcoming.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_generic));
            }
        });
    }

    private boolean hasLocationPermission() {
        Context context = getContext();
        if (context == null) return false;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void showLocationRationaleDialog() {
        if (!isSafe()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.guide_gps_broadcast_title)
                .setMessage(R.string.guide_gps_permission_rationale)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    locationPermissionLauncher.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    binding.switchGpsBroadcast.setChecked(false);
                    saveGpsPref(false);
                })
                .setCancelable(false)
                .show();
    }

    private void showPermissionDeniedDialog() {
        if (!isSafe()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_location_permission_need)
                .setMessage(R.string.guide_gps_permission_denied)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void startGpsBroadcast() {
        isBroadcasting = true;
        saveGpsPref(true);
        if (binding != null) {
            binding.switchGpsBroadcast.setChecked(true);
            binding.tvGpsStatusDesc.setText(R.string.guide_gps_broadcast_desc);
        }
        broadcastHandler.removeCallbacks(broadcastRunnable);
        broadcastHandler.post(broadcastRunnable);
    }

    private void stopGpsBroadcast() {
        isBroadcasting = false;
        broadcastHandler.removeCallbacks(broadcastRunnable);
    }

    private void broadcastCurrentLocation() {
        if (!isSafe() || guideId <= 0 || !hasLocationPermission()) return;

        Context context = getContext();
        if (context == null) return;

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) return;

        Location location = null;
        try {
            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (location == null) {
                location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
        } catch (SecurityException ignored) {
            return;
        }

        double lat = location != null ? location.getLatitude() : 23.8103;
        double lng = location != null ? location.getLongitude() : 90.4125;

        UpdateLocationRequest req = new UpdateLocationRequest(lat, lng);
        ApiClient.getApiService().updateGuideLocation(guideId, req).enqueue(new ApiCallback<com.example.horizontravel.data.model.Location>() {
            @Override
            public void onSuccess(com.example.horizontravel.data.model.Location loc) {
                if (!isSafe()) return;
                // Location successfully synced to backend
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                // Silently ignore single location update error
            }
        });
    }

    private void saveGpsPref(boolean enabled) {
        Context context = getContext();
        if (context == null) return;
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putBoolean(PREF_KEY_GPS_PREFIX + guideId, enabled).apply();
    }

    private boolean isGpsPrefEnabled() {
        Context context = getContext();
        if (context == null) return false;
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(PREF_KEY_GPS_PREFIX + guideId, false);
    }
}
