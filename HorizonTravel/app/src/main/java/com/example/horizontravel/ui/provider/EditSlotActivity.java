package com.example.horizontravel.ui.provider;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.data.model.UpdateServiceSlotRequest;
import com.example.horizontravel.databinding.ActivityEditSlotBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Formatters;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EditSlotActivity extends BaseActivity {

    private ActivityEditSlotBinding binding;
    private long slotId = -1;

    private static final String[] STATUSES = new String[] {
            Constants.SlotStatus.OPEN,
            Constants.SlotStatus.FULL,
            Constants.SlotStatus.CLOSED,
            Constants.SlotStatus.CANCELLED
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.horizontravel.util.SessionManager session = com.example.horizontravel.util.SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.SERVICE_PROVIDER.equalsIgnoreCase(session.getRole())) {
            finish();
            return;
        }

        binding = ActivityEditSlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        slotId = getIntent().getLongExtra("EXTRA_SLOT_ID", -1);
        if (slotId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_slot_id, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupStatusDropdown();
        populateInitialData();

        binding.etStartDateTime.setOnClickListener(v -> pickDateTime(true));
        binding.tilStartDateTime.setEndIconOnClickListener(v -> pickDateTime(true));

        binding.etEndDateTime.setOnClickListener(v -> pickDateTime(false));
        binding.tilEndDateTime.setEndIconOnClickListener(v -> pickDateTime(false));

        binding.btnUpdateSlot.setOnClickListener(v -> {
            if (isSingleClick()) {
                submitUpdate();
            }
        });

        binding.btnDeleteSlot.setOnClickListener(v -> {
            if (isSingleClick()) {
                confirmDelete();
            }
        });
    }

    private void setupStatusDropdown() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, STATUSES);
        binding.actStatus.setAdapter(statusAdapter);
    }

    private void populateInitialData() {
        String category = getIntent().getStringExtra("EXTRA_CATEGORY");
        if (category != null && !category.isEmpty()) {
            binding.tvCategoryLabel.setText(getString(R.string.provider_category_label, Formatters.capitalizeEnum(category)));
        }

        String status = getIntent().getStringExtra("EXTRA_STATUS");
        if (status != null && !status.isEmpty()) {
            binding.actStatus.setText(status, false);
        } else {
            binding.actStatus.setText(Constants.SlotStatus.OPEN, false);
        }

        binding.etOrigin.setText(getIntent().getStringExtra("EXTRA_ORIGIN"));
        binding.etDestination.setText(getIntent().getStringExtra("EXTRA_DESTINATION"));
        binding.etLocationName.setText(getIntent().getStringExtra("EXTRA_LOCATION_NAME"));
        binding.etStartDateTime.setText(getIntent().getStringExtra("EXTRA_START_DATE_TIME"));
        binding.etEndDateTime.setText(getIntent().getStringExtra("EXTRA_END_DATE_TIME"));

        int capacity = getIntent().getIntExtra("EXTRA_CAPACITY", 0);
        if (capacity > 0) {
            binding.etCapacity.setText(String.valueOf(capacity));
        }

        double price = getIntent().getDoubleExtra("EXTRA_PRICE", 0.0);
        if (price > 0.0) {
            binding.etPrice.setText(String.valueOf(price));
        }
    }

    private void pickDateTime(boolean isStart) {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStart ? R.string.slot_start_time_hint : R.string.slot_end_time_hint)
                .setCalendarConstraints(constraints)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String datePart = sdf.format(new Date(selection));

                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(isStart ? 9 : 17)
                        .setMinute(0)
                        .setTitleText(isStart ? R.string.slot_start_time_hint : R.string.slot_end_time_hint)
                        .build();

                timePicker.addOnPositiveButtonClickListener(v -> {
                    String timePart = String.format(Locale.US, "%02d:%02d:00",
                            timePicker.getHour(), timePicker.getMinute());
                    String isoDateTime = datePart + "T" + timePart;

                    if (isStart) {
                        binding.etStartDateTime.setText(isoDateTime);
                        binding.tilStartDateTime.setError(null);
                    } else {
                        binding.etEndDateTime.setText(isoDateTime);
                        binding.tilEndDateTime.setError(null);
                    }
                });

                timePicker.show(getSupportFragmentManager(), isStart ? "START_TIME_PICKER" : "END_TIME_PICKER");
            }
        });

        datePicker.show(getSupportFragmentManager(), isStart ? "START_DATE_PICKER" : "END_DATE_PICKER");
    }

    private void submitUpdate() {
        clearErrors();

        String origin = binding.etOrigin.getText() != null ? binding.etOrigin.getText().toString().trim() : "";
        String destination = binding.etDestination.getText() != null ? binding.etDestination.getText().toString().trim() : "";
        String locationName = binding.etLocationName.getText() != null ? binding.etLocationName.getText().toString().trim() : "";

        if ((origin.isEmpty() || destination.isEmpty()) && locationName.isEmpty()) {
            binding.tilLocationName.setError(getString(R.string.error_slot_location_or_route));
            return;
        }

        String startDateTime = binding.etStartDateTime.getText() != null ? binding.etStartDateTime.getText().toString().trim() : "";
        if (startDateTime.isEmpty()) {
            binding.tilStartDateTime.setError(getString(R.string.error_field_required));
            return;
        }

        String endDateTime = binding.etEndDateTime.getText() != null ? binding.etEndDateTime.getText().toString().trim() : "";
        if (endDateTime.isEmpty()) {
            binding.tilEndDateTime.setError(getString(R.string.error_field_required));
            return;
        }

        if (endDateTime.compareTo(startDateTime) <= 0) {
            binding.tilEndDateTime.setError(getString(R.string.error_slot_dates_invalid));
            return;
        }

        String capStr = binding.etCapacity.getText() != null ? binding.etCapacity.getText().toString().trim() : "";
        int capacity = 0;
        try {
            capacity = Integer.parseInt(capStr);
        } catch (NumberFormatException ignored) {}
        if (capacity <= 0) {
            binding.tilCapacity.setError(getString(R.string.error_slot_capacity_invalid));
            return;
        }

        String priceStr = binding.etPrice.getText() != null ? binding.etPrice.getText().toString().trim() : "";
        double price = 0.0;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException ignored) {}
        if (price <= 0.0) {
            binding.tilPrice.setError(getString(R.string.error_slot_price_invalid));
            return;
        }

        String status = binding.actStatus.getText() != null ? binding.actStatus.getText().toString().trim() : Constants.SlotStatus.OPEN;

        UpdateServiceSlotRequest request = new UpdateServiceSlotRequest();
        request.setOrigin(origin.isEmpty() ? null : origin);
        request.setDestination(destination.isEmpty() ? null : destination);
        request.setLocationName(locationName.isEmpty() ? null : locationName);
        request.setStartDateTime(startDateTime);
        request.setEndDateTime(endDateTime);
        request.setCapacity(capacity);
        request.setPrice(price);
        request.setStatus(status);

        setLoading(true);

        ApiClient.getApiService().updateSlot(slotId, request).enqueue(new ApiCallback<ServiceSlot>() {
            @Override
            public void onSuccess(ServiceSlot response) {
                if (!isSafe()) return;
                setLoading(false);
                Toast.makeText(EditSlotActivity.this, R.string.slot_updated_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                setLoading(false);
                Toast.makeText(EditSlotActivity.this, message != null ? message : getString(R.string.error_network), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete_slot_title)
                .setMessage(R.string.confirm_delete_slot_msg)
                .setPositiveButton(R.string.btn_delete_slot, (dialog, which) -> deleteSlot())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteSlot() {
        setLoading(true);

        ApiClient.getApiService().deleteSlot(slotId).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                if (!isSafe()) return;
                setLoading(false);
                Toast.makeText(EditSlotActivity.this, R.string.slot_deleted_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                setLoading(false);
                Toast.makeText(EditSlotActivity.this, message != null ? message : getString(R.string.error_network), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearErrors() {
        binding.tilOrigin.setError(null);
        binding.tilDestination.setError(null);
        binding.tilLocationName.setError(null);
        binding.tilStartDateTime.setError(null);
        binding.tilEndDateTime.setError(null);
        binding.tilCapacity.setError(null);
        binding.tilPrice.setError(null);
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnUpdateSlot.setEnabled(!loading);
        binding.btnDeleteSlot.setEnabled(!loading);
    }
}
