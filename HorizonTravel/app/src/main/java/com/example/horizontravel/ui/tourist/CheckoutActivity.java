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
import com.example.horizontravel.data.model.Booking;
import com.example.horizontravel.data.model.BookingRequest;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Payment;
import com.example.horizontravel.data.model.PaymentRequest;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.ActivityCheckoutBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.util.CartManager;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends BaseActivity {

    private ActivityCheckoutBinding binding;
    private List<ServiceSlot> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (!SessionManager.getInstance().isLoggedIn()) {
            Toast.makeText(this, R.string.dialog_sign_in_required_msg, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        cartItems = CartManager.getInstance().getItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, R.string.toast_your_cart_is_empty, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        displaySummary();
        setupListeners();
    }

    private void displaySummary() {
        binding.layoutCheckoutItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        double total = 0.0;

        for (ServiceSlot slot : cartItems) {
            com.example.horizontravel.databinding.ItemTwoLineBinding itemBinding =
                    com.example.horizontravel.databinding.ItemTwoLineBinding.inflate(inflater, binding.layoutCheckoutItems, false);

            String title = slot.getLocationName();
            if (title == null || title.trim().isEmpty()) {
                if (slot.getOrigin() != null && slot.getDestination() != null) {
                    title = slot.getOrigin() + " → " + slot.getDestination();
                } else {
                    title = slot.getCategory() != null ? slot.getCategory() : "Service Slot";
                }
            }
            itemBinding.tvText1.setText(title);
            itemBinding.tvText1.setTextColor(getResources().getColor(R.color.on_surface));

            itemBinding.tvText2.setText(String.format(java.util.Locale.US, "%s  •  %s",
                    slot.getCategory() != null ? slot.getCategory() : "",
                    Formatters.formatCurrency(slot.getPrice())));
            itemBinding.tvText2.setTextColor(getResources().getColor(R.color.navy_primary));

            binding.layoutCheckoutItems.addView(itemBinding.getRoot());
            total += slot.getPrice();
        }

        binding.tvOrderTotal.setText(Formatters.formatCurrency(total));
    }

    private void setupListeners() {
        binding.btnConfirmPay.setOnClickListener(v -> {
            String method = Constants.PaymentMethod.CARD;
            int checkedId = binding.rgPaymentMethods.getCheckedRadioButtonId();
            if (checkedId == R.id.rbMobileBanking) {
                method = Constants.PaymentMethod.MOBILE_BANKING;
            } else if (checkedId == R.id.rbCash) {
                method = Constants.PaymentMethod.CASH;
            }
            startSequentialCheckout(method);
        });
    }

    private void startSequentialCheckout(String method) {
        binding.btnConfirmPay.setEnabled(false);
        binding.layoutProcessing.setVisibility(View.VISIBLE);
        binding.rgPaymentMethods.setEnabled(false);

        List<ServiceSlot> itemsToProcess = new ArrayList<>(cartItems);
        processItem(0, itemsToProcess, method, new ArrayList<>(), new ArrayList<>());
    }

    private void processItem(int index, List<ServiceSlot> items, String method,
                            List<Long> successBookingIds, List<String> errorMessages) {
        if (index >= items.size()) {
            onAllProcessed(items.size(), successBookingIds, errorMessages);
            return;
        }

        ServiceSlot slot = items.get(index);
        binding.tvProcessingStatus.setText(
                String.format(java.util.Locale.US, "Processing item %d of %d…", index + 1, items.size()));

        long touristId = SessionManager.getInstance().getUserId();
        BookingRequest bookingRequest = new BookingRequest(touristId, slot.getId());

        ApiClient.getApiService().createBooking(bookingRequest).enqueue(new ApiCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                if (isFinishing() || isDestroyed()) return;

                if (booking != null && booking.getId() != null) {
                    long bookingId = booking.getId();

                    // Create payment for this booking
                    PaymentRequest paymentRequest = new PaymentRequest(
                            bookingId,
                            null,
                            slot.getPrice(),
                            method
                    );

                    ApiClient.getApiService().createPayment(paymentRequest).enqueue(new ApiCallback<Payment>() {
                        @Override
                        public void onSuccess(Payment payment) {
                            if (isFinishing() || isDestroyed()) return;
                            successBookingIds.add(bookingId);
                            CartManager.getInstance().removeItem(slot.getId());
                            processItem(index + 1, items, method, successBookingIds, errorMessages);
                        }

                        @Override
                        public void onError(String message, int statusCode) {
                            if (isFinishing() || isDestroyed()) return;
                            errorMessages.add("Slot #" + slot.getId() + " payment failed: " + (message != null ? message : "Payment Error"));
                            processItem(index + 1, items, method, successBookingIds, errorMessages);
                        }
                    });
                } else {
                    errorMessages.add("Slot #" + slot.getId() + ": Empty booking response");
                    processItem(index + 1, items, method, successBookingIds, errorMessages);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                errorMessages.add("Slot #" + slot.getId() + ": " + (message != null ? message : "Booking failed"));
                processItem(index + 1, items, method, successBookingIds, errorMessages);
            }
        });
    }

    private void onAllProcessed(int totalItems, List<Long> successBookingIds, List<String> errorMessages) {
        binding.layoutProcessing.setVisibility(View.GONE);

        Intent intent = new Intent(this, BookingConfirmationActivity.class);
        intent.putExtra(BookingConfirmationActivity.EXTRA_TOTAL_COUNT, totalItems);
        intent.putExtra(BookingConfirmationActivity.EXTRA_SUCCESS_COUNT, successBookingIds.size());
        intent.putExtra(BookingConfirmationActivity.EXTRA_FAILED_COUNT, errorMessages.size());

        long[] idsArray = new long[successBookingIds.size()];
        for (int i = 0; i < successBookingIds.size(); i++) {
            idsArray[i] = successBookingIds.get(i);
        }
        intent.putExtra(BookingConfirmationActivity.EXTRA_BOOKING_IDS, idsArray);

        startActivity(intent);
        finish();
    }
}
