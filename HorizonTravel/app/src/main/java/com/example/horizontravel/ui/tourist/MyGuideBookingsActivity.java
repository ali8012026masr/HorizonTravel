package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.GuideBooking;
import com.example.horizontravel.data.model.Payment;
import com.example.horizontravel.data.model.PaymentRequest;
import com.example.horizontravel.data.model.UpdateGuideBookingStatusRequest;
import com.example.horizontravel.databinding.ActivityMyGuideBookingsBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyGuideBookingsActivity extends BaseActivity {

    private ActivityMyGuideBookingsBinding binding;
    private GuideBookingAdapter adapter;
    private final List<GuideBooking> allBookings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyGuideBookingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        binding.etSearchGuideBookings.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        setupListeners();
        loadBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }

    private void setupRecyclerView() {
        adapter = new GuideBookingAdapter(new GuideBookingAdapter.OnGuideBookingActionListener() {
            @Override
            public void onBookingClick(GuideBooking booking) {
                if (booking != null && booking.getId() != null) {
                    Intent intent = new Intent(MyGuideBookingsActivity.this, GuideBookingDetailActivity.class);
                    intent.putExtra(GuideBookingDetailActivity.EXTRA_GUIDE_BOOKING_ID, booking.getId());
                    startActivity(intent);
                }
            }

            @Override
            public void onViewLocation(GuideBooking booking) {
                if (booking != null && booking.getTourGuideId() != null) {
                    Intent intent = new Intent(MyGuideBookingsActivity.this, GuideLocationActivity.class);
                    intent.putExtra(GuideLocationActivity.EXTRA_GUIDE_ID, booking.getTourGuideId());
                    startActivity(intent);
                }
            }

            @Override
            public void onRateGuide(GuideBooking booking) {
                if (booking != null && booking.getId() != null) {
                    Intent intent = new Intent(MyGuideBookingsActivity.this, RateGuideActivity.class);
                    intent.putExtra(RateGuideActivity.EXTRA_GUIDE_BOOKING_ID, booking.getId());
                    startActivity(intent);
                }
            }
        });

        binding.rvGuideBookings.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGuideBookings.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadBookings);
    }

    private void loadBookings() {
        long userId = SessionManager.getInstance().getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);

        // Pre-fetch guides to enrich guide bookings with human-readable guide names
        ApiClient.getApiService().getGuides(null).enqueue(new ApiCallback<List<Guide>>() {
            @Override
            public void onSuccess(List<Guide> guides) {
                if (isFinishing() || isDestroyed()) return;
                if (guides != null) {
                    Map<Long, Guide> map = new HashMap<>();
                    for (Guide g : guides) {
                        if (g != null && g.getId() != null) {
                            map.put(g.getId(), g);
                        }
                    }
                    adapter.setGuideCache(map);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                // Non-fatal: individual guides will still be queried on-demand by the adapter
            }
        });

        ApiClient.getApiService().getGuideBookingsForTourist(userId).enqueue(new ApiCallback<List<GuideBooking>>() {
            @Override
            public void onSuccess(List<GuideBooking> bookings) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (bookings == null || bookings.isEmpty()) {
                    allBookings.clear();
                    adapter.setBookings(new ArrayList<>());
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    allBookings.clear();
                    allBookings.addAll(bookings);
                    applySearch(binding.etSearchGuideBookings.getText());
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(MyGuideBookingsActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applySearch(CharSequence query) {
        String term = query == null ? "" : query.toString().trim().toLowerCase(java.util.Locale.US);
        List<GuideBooking> filtered = new ArrayList<>();
        for (GuideBooking booking : allBookings) {
            String searchable = ((booking.getStatus() == null ? "" : booking.getStatus()) + " "
                    + (booking.getScheduleDate() == null ? "" : booking.getScheduleDate()) + " "
                    + booking.getId()).toLowerCase(java.util.Locale.US);
            if (term.isEmpty() || searchable.contains(term)) {
                filtered.add(booking);
            }
        }
        adapter.setBookings(filtered);
        binding.layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showPayGuideDialog(GuideBooking booking) {
        if (booking == null || booking.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_pay_guide_fee)
                .setMessage(String.format(java.util.Locale.US, "Confirm payment of $%.2f for this guide booking?", booking.getAgreedPrice()))
                .setPositiveButton("Pay via Card", (dialog, which) -> {
                    processGuidePayment(booking, Constants.PaymentMethod.CARD);
                })
                .setNeutralButton("Pay via Mobile Banking", (dialog, which) -> {
                    processGuidePayment(booking, Constants.PaymentMethod.MOBILE_BANKING);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void processGuidePayment(GuideBooking booking, String method) {
        PaymentRequest paymentRequest = new PaymentRequest(
                null,
                booking.getId(),
                booking.getAgreedPrice(),
                method
        );

        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().createPayment(paymentRequest).enqueue(new ApiCallback<Payment>() {
            @Override
            public void onSuccess(Payment payment) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyGuideBookingsActivity.this, R.string.toast_payment_processed_succes, Toast.LENGTH_SHORT).show();
                loadBookings();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyGuideBookingsActivity.this, message != null ? message : "Payment failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCancelGuideBookingDialog(GuideBooking booking) {
        if (booking == null || booking.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_cancel_guide_booking_title)
                .setMessage(R.string.confirm_cancel_guide_booking_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> cancelGuideBooking(booking))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void cancelGuideBooking(GuideBooking booking) {
        if (booking == null || booking.getId() == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().updateGuideBookingStatus(
                booking.getId(),
                new UpdateGuideBookingStatusRequest(Constants.GuideBookingStatus.DECLINED)
        ).enqueue(new ApiCallback<GuideBooking>() {
            @Override
            public void onSuccess(GuideBooking data) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyGuideBookingsActivity.this, R.string.guide_booking_cancelled_success, Toast.LENGTH_SHORT).show();
                loadBookings();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MyGuideBookingsActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
