package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.horizontravel.R;
import com.example.horizontravel.databinding.ActivityBookingConfirmationBinding;
import com.example.horizontravel.ui.BaseActivity;

public class BookingConfirmationActivity extends BaseActivity {

    public static final String EXTRA_TOTAL_COUNT = "extra_total_count";
    public static final String EXTRA_SUCCESS_COUNT = "extra_success_count";
    public static final String EXTRA_FAILED_COUNT = "extra_failed_count";
    public static final String EXTRA_BOOKING_IDS = "extra_booking_ids";

    private ActivityBookingConfirmationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int totalCount = getIntent().getIntExtra(EXTRA_TOTAL_COUNT, 0);
        int successCount = getIntent().getIntExtra(EXTRA_SUCCESS_COUNT, 0);
        int failedCount = getIntent().getIntExtra(EXTRA_FAILED_COUNT, 0);
        long[] bookingIds = getIntent().getLongArrayExtra(EXTRA_BOOKING_IDS);

        displayStatus(totalCount, successCount, failedCount, bookingIds);
        setupListeners();
    }

    private void displayStatus(int total, int success, int failed, long[] bookingIds) {
        if (failed == 0 && success > 0) {
            // All success
            binding.ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
            binding.tvConfirmationTitle.setText(R.string.confirmation_success_title);
            binding.tvConfirmationMessage.setText(
                    getString(R.string.confirmation_msg_all_success, success));
        } else if (success > 0 && failed > 0) {
            // Partial
            binding.ivStatusIcon.setImageResource(R.drawable.ic_error);
            binding.tvConfirmationTitle.setText(R.string.confirmation_partial_title);
            binding.tvConfirmationMessage.setText(
                    getString(R.string.confirmation_msg_partial, success, failed));
        } else {
            // Failed
            binding.ivStatusIcon.setImageResource(R.drawable.ic_error);
            binding.tvConfirmationTitle.setText(R.string.confirmation_failed_title);
            binding.tvConfirmationMessage.setText(R.string.confirmation_msg_failed);
        }

        if (bookingIds != null && bookingIds.length > 0) {
            StringBuilder sb = new StringBuilder("Confirmed Bookings: ");
            for (int i = 0; i < bookingIds.length; i++) {
                sb.append("#").append(bookingIds[i]);
                if (i < bookingIds.length - 1) sb.append(", ");
            }
            binding.tvBookingIds.setText(sb.toString());
            binding.tvBookingIds.setVisibility(View.VISIBLE);
        } else {
            binding.tvBookingIds.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        binding.btnViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, TouristMainActivity.class);
            intent.putExtra(TouristMainActivity.EXTRA_TARGET_TAB, R.id.nav_bookings);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, TouristMainActivity.class);
            intent.putExtra(TouristMainActivity.EXTRA_TARGET_TAB, R.id.nav_home);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
