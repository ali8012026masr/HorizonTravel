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
import com.example.horizontravel.data.model.CreateServiceSlotRequest;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.ActivityCreateSlotBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CreateSlotActivity extends BaseActivity {

    private ActivityCreateSlotBinding binding;

    private static final String[] CATEGORIES = new String[] {
            Constants.ServiceCategory.BUS,
            Constants.ServiceCategory.MICROBUS,
            Constants.ServiceCategory.LAUNCH,
            Constants.ServiceCategory.TRAIN,
            Constants.ServiceCategory.AIRPLANE,
            Constants.ServiceCategory.SHIP,
            Constants.ServiceCategory.HOTEL,
            Constants.ServiceCategory.RESORT,
            Constants.ServiceCategory.CONVENTION_CENTER,
            Constants.ServiceCategory.BUFFET,
            Constants.ServiceCategory.AMUSEMENT_PARK
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.horizontravel.util.SessionManager session = com.example.horizontravel.util.SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.SERVICE_PROVIDER.equalsIgnoreCase(session.getRole())) {
            finish();
            return;
        }

        binding = ActivityCreateSlotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        binding.actCategory.setAdapter(categoryAdapter);

        // Date-Time pickers
        binding.etStartDateTime.setOnClickListener(v -> pickDateTime(true));
        binding.tilStartDateTime.setEndIconOnClickListener(v -> pickDateTime(true));

        binding.etEndDateTime.setOnClickListener(v -> pickDateTime(false));
        binding.tilEndDateTime.setEndIconOnClickListener(v -> pickDateTime(false));

        binding.btnCreateSlot.setOnClickListener(v -> {
            if (isSingleClick()) {
                submitSlot();
            }
        });
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

                // Now show TimePicker
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

    private void submitSlot() {
        clearErrors();

        long providerId = SessionManager.getInstance().getUserId();
        if (providerId <= 0) {
            Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String category = binding.actCategory.getText() != null ? binding.actCategory.getText().toString().trim() : "";
        if (category.isEmpty()) {
            binding.tilCategory.setError(getString(R.string.error_slot_category_required));
            return;
        }

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

        CreateServiceSlotRequest request = new CreateServiceSlotRequest(
                providerId,
                category,
                origin.isEmpty() ? null : origin,
                destination.isEmpty() ? null : destination,
                locationName.isEmpty() ? null : locationName,
                startDateTime,
                endDateTime,
                capacity,
                price
        );

        setLoading(true);

        ApiClient.getApiService().createSlot(request).enqueue(new ApiCallback<ServiceSlot>() {
            @Override
            public void onSuccess(ServiceSlot slot) {
                if (!isSafe()) return;
                setLoading(false);
                Toast.makeText(CreateSlotActivity.this, R.string.slot_created_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                setLoading(false);
                Toast.makeText(CreateSlotActivity.this, message != null ? message : getString(R.string.error_slot_dates_invalid), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearErrors() {
        binding.tilCategory.setError(null);
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
        binding.btnCreateSlot.setEnabled(!loading);
    }
}
