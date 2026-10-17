package com.example.horizontravel.ui.provider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Booking;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Payment;
import com.example.horizontravel.data.model.StatusRequest;
import com.example.horizontravel.databinding.FragmentProviderBookingsBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ProviderBookingsFragment extends BaseFragment<FragmentProviderBookingsBinding> {

    private ProviderPaymentAdapter paymentAdapter;
    private ProviderBookingAdapter bookingAdapter;

    @Override
    protected FragmentProviderBookingsBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProviderBookingsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapters();

        binding.swipeRefresh.setOnRefreshListener(this::loadBookings);

        binding.btnLookup.setOnClickListener(v -> {
            if (isSingleClick()) {
                performLookup();
            }
        });

        loadBookings();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookings();
    }

    private void setupAdapters() {
        paymentAdapter = new ProviderPaymentAdapter();
        binding.rvLookupPayments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLookupPayments.setAdapter(paymentAdapter);

        bookingAdapter = new ProviderBookingAdapter(booking -> {
            if (isSingleClick() && booking != null) {
                showBookingActionsDialog(booking);
            }
        });
        binding.rvProviderBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvProviderBookings.setAdapter(bookingAdapter);
    }

    private void performLookup() {
        binding.tilBookingId.setError(null);
        String input = binding.etBookingId.getText() != null ? binding.etBookingId.getText().toString().trim() : "";
        if (input.isEmpty()) {
            binding.tilBookingId.setError(getString(R.string.error_field_required));
            return;
        }

        long bookingId;
        try {
            bookingId = Long.parseLong(input);
        } catch (NumberFormatException e) {
            binding.tilBookingId.setError(getString(R.string.error_field_required));
            return;
        }

        lookupPaymentsForBooking(bookingId);
    }

    private void lookupPaymentsForBooking(long bookingId) {
        binding.pbLookup.setVisibility(View.VISIBLE);
        binding.layoutLookupResults.setVisibility(View.GONE);
        binding.tvNoPaymentsFound.setVisibility(View.GONE);

        ApiClient.getApiService().getPaymentsForBooking(bookingId).enqueue(new ApiCallback<List<Payment>>() {
            @Override
            public void onSuccess(List<Payment> payments) {
                if (!isSafe()) return;
                binding.pbLookup.setVisibility(View.GONE);
                binding.layoutLookupResults.setVisibility(View.VISIBLE);
                binding.tvLookupResultTitle.setText(getString(R.string.provider_payments_for_booking_title, bookingId));

                if (payments != null && !payments.isEmpty()) {
                    paymentAdapter.setItems(payments);
                    binding.rvLookupPayments.setVisibility(View.VISIBLE);
                    binding.tvNoPaymentsFound.setVisibility(View.GONE);
                } else {
                    paymentAdapter.setItems(null);
                    binding.rvLookupPayments.setVisibility(View.GONE);
                    binding.tvNoPaymentsFound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.pbLookup.setVisibility(View.GONE);
                binding.layoutLookupResults.setVisibility(View.VISIBLE);
                binding.tvLookupResultTitle.setText(getString(R.string.provider_payments_for_booking_title, bookingId));
                binding.rvLookupPayments.setVisibility(View.GONE);
                binding.tvNoPaymentsFound.setVisibility(View.VISIBLE);
                showError(message);
            }
        });
    }

    private void showBookingActionsDialog(Booking booking) {
        String[] options = new String[] {
                "Verify Payments for Booking #" + booking.getId(),
                "Mark as Completed",
                "Cancel Booking"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.booking_id_item_format, booking.getId()))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        binding.etBookingId.setText(String.valueOf(booking.getId()));
                        lookupPaymentsForBooking(booking.getId());
                    } else if (which == 1) {
                        updateBookingStatus(booking.getId(), Constants.BookingStatus.COMPLETED);
                    } else if (which == 2) {
                        updateBookingStatus(booking.getId(), Constants.BookingStatus.CANCELLED);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateBookingStatus(long bookingId, String newStatus) {
        binding.pbBookings.setVisibility(View.VISIBLE);
        StatusRequest request = new StatusRequest(newStatus);
        ApiClient.getApiService().updateBookingStatus(bookingId, request).enqueue(new ApiCallback<Booking>() {
            @Override
            public void onSuccess(Booking updated) {
                if (!isSafe()) return;
                binding.pbBookings.setVisibility(View.GONE);
                showToast("Booking #" + bookingId + " status updated to " + newStatus);
                loadBookings();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.pbBookings.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void loadBookings() {
        // Gap in backend: No API exists to fetch bookings for a provider's slots directly.
        // Fallback to manual lookup only.
        binding.swipeRefresh.setRefreshing(false);
        binding.pbBookings.setVisibility(View.GONE);
        binding.rvProviderBookings.setVisibility(View.GONE);
        binding.layoutBookingsEmpty.setVisibility(View.GONE);
    }
}
