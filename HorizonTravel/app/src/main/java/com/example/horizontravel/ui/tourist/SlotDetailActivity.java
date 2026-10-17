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
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.ServiceRating;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.ActivitySlotDetailBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.auth.RegisterChooserActivity;
import com.example.horizontravel.util.CartManager;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Locale;

public class SlotDetailActivity extends BaseActivity {

    public static final String EXTRA_SLOT_ID = "extra_slot_id";

    private ActivitySlotDetailBinding binding;
    private long slotId;
    private ServiceSlot currentSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySlotDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        slotId = getIntent().getLongExtra(EXTRA_SLOT_ID, -1);
        if (slotId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_service_slot, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setupButtons();
        loadSlotDetails();
    }

    private void setupButtons() {
        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentSlot == null) return;
            if (!SessionManager.getInstance().isLoggedIn()) {
                showSignInRequiredDialog();
                return;
            }
            if (!Constants.Role.TOURIST.equalsIgnoreCase(SessionManager.getInstance().getRole())) {
                Toast.makeText(this, R.string.only_tourists_can_book, Toast.LENGTH_SHORT).show();
                return;
            }
            boolean added = CartManager.getInstance().addItem(currentSlot);
            if (added) {
                Toast.makeText(this, R.string.slot_added_to_cart, Toast.LENGTH_SHORT).show();
                updateCartButtonState();
            } else {
                Toast.makeText(this, R.string.slot_already_in_cart, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBookNow.setOnClickListener(v -> {
            if (currentSlot == null) return;
            if (!SessionManager.getInstance().isLoggedIn()) {
                showSignInRequiredDialog();
                return;
            }
            if (!Constants.Role.TOURIST.equalsIgnoreCase(SessionManager.getInstance().getRole())) {
                Toast.makeText(this, R.string.only_tourists_can_book, Toast.LENGTH_SHORT).show();
                return;
            }
            CartManager.getInstance().addItem(currentSlot);
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    private void showSignInRequiredDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_sign_in_required_title)
                .setMessage(R.string.dialog_sign_in_required_msg)
                .setPositiveButton(R.string.dialog_sign_in_button, (dialog, which) -> {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                })
                .setNeutralButton(R.string.dialog_register_button, (dialog, which) -> {
                    Intent intent = new Intent(this, RegisterChooserActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateCartButtonState() {
        if (currentSlot == null || currentSlot.getId() == null) return;
        boolean inCart = CartManager.getInstance().contains(currentSlot.getId());
        if (inCart) {
            binding.btnAddToCart.setText(R.string.slot_already_in_cart);
            binding.btnAddToCart.setEnabled(false);
        } else {
            binding.btnAddToCart.setText(R.string.slot_add_to_cart);
            binding.btnAddToCart.setEnabled(true);
        }
    }

    private void loadSlotDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getSlotById(slotId).enqueue(new ApiCallback<ServiceSlot>() {
            @Override
            public void onSuccess(ServiceSlot slot) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                currentSlot = slot;
                displaySlotInfo(slot);
                updateCartButtonState();
                if (slot.getProviderId() != null) {
                    loadProviderRatings(slot.getProviderId());
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(SlotDetailActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displaySlotInfo(ServiceSlot slot) {
        if (slot == null) return;

        binding.tvCategory.setText(slot.getCategory() != null ? slot.getCategory() : "SERVICE");
        binding.tvStatus.setText(slot.getStatus() != null ? slot.getStatus() : "OPEN");

        String title = slot.getLocationName();
        if (title == null || title.trim().isEmpty()) {
            if (slot.getOrigin() != null && slot.getDestination() != null) {
                title = slot.getOrigin() + " → " + slot.getDestination();
            } else {
                title = slot.getCategory() != null ? slot.getCategory() : "Service Slot";
            }
        }
        binding.tvTitle.setText(title);

        String location = "";
        if (slot.getOrigin() != null && slot.getDestination() != null) {
            location = "From: " + slot.getOrigin() + "  |  To: " + slot.getDestination();
        } else if (slot.getLocationName() != null) {
            location = slot.getLocationName();
        }
        binding.tvLocation.setText(location);
        binding.tvLocation.setVisibility(location.isEmpty() ? View.GONE : View.VISIBLE);

        binding.tvSeats.setText(getString(R.string.slot_available_seats, slot.getAvailableSeats()));
        binding.tvPrice.setText(Formatters.formatCurrency(slot.getPrice()));

        String dates = "Start: " + Formatters.formatDateTime(slot.getStartDateTime())
                + "\nEnd: " + Formatters.formatDateTime(slot.getEndDateTime());
        binding.tvDates.setText(dates);

        if (slot.getProviderBusinessName() != null && !slot.getProviderBusinessName().isEmpty()) {
            binding.tvProviderBusinessName.setText(slot.getProviderBusinessName());
        } else {
            binding.tvProviderBusinessName.setText(R.string.text_verified_provider);
        }

        if (slot.getProviderRatingAvg() != null && slot.getProviderRatingCount() != null && slot.getProviderRatingCount() > 0) {
            binding.layoutProviderRating.setVisibility(View.VISIBLE);
            binding.tvProviderRating.setText(String.format(Locale.US, "%.1f / 5.0 (%d reviews)",
                    slot.getProviderRatingAvg(), slot.getProviderRatingCount()));
        } else {
            binding.layoutProviderRating.setVisibility(View.GONE);
        }
    }

    private void loadProviderRatings(long providerId) {
        ApiClient.getApiService().getRatingsForProvider(providerId).enqueue(new ApiCallback<List<ServiceRating>>() {
            @Override
            public void onSuccess(List<ServiceRating> ratings) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutReviewsContainer.removeAllViews();

                if (ratings == null || ratings.isEmpty()) {
                    binding.tvNoReviews.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoReviews.setVisibility(View.GONE);
                    LayoutInflater inflater = LayoutInflater.from(SlotDetailActivity.this);
                    for (ServiceRating r : ratings) {
                        com.example.horizontravel.databinding.ItemTwoLineBinding itemBinding =
                                com.example.horizontravel.databinding.ItemTwoLineBinding.inflate(inflater, binding.layoutReviewsContainer, false);

                        itemBinding.tvText1.setText(String.format(Locale.US, "★ %d/5  •  %s",
                                r.getScore(),
                                r.getRatedDate() != null ? Formatters.formatDate(r.getRatedDate()) : ""));
                        itemBinding.tvText1.setTextColor(getResources().getColor(R.color.navy_primary));

                        String comment = (r.getComment() != null && !r.getComment().trim().isEmpty())
                                ? r.getComment() : "No comment provided.";
                        itemBinding.tvText2.setText(comment);
                        itemBinding.tvText2.setTextColor(getResources().getColor(R.color.on_surface_variant));

                        binding.layoutReviewsContainer.addView(itemBinding.getRoot());
                    }
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.tvNoReviews.setVisibility(View.VISIBLE);
            }
        });
    }
}
