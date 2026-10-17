package com.example.horizontravel.ui.guide;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.CreateGuideAvailabilityRequest;
import com.example.horizontravel.data.model.GuideAvailability;
import com.example.horizontravel.databinding.ActivityCreateAvailabilityBinding;
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

public class CreateAvailabilityActivity extends BaseActivity {

    private ActivityCreateAvailabilityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.horizontravel.util.SessionManager session = com.example.horizontravel.util.SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.TOUR_GUIDE.equalsIgnoreCase(session.getRole())) {
            finish();
            return;
        }

        binding = ActivityCreateAvailabilityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupPickers();
        binding.btnSubmit.setOnClickListener(v -> submitAvailability());
    }

    private void setupPickers() {
        // Date picker
        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.tilDate.setEndIconOnClickListener(v -> showDatePicker());

        // Start Time picker
        binding.etStartTime.setOnClickListener(v -> showTimePicker(true));
        binding.tilStartTime.setEndIconOnClickListener(v -> showTimePicker(true));

        // End Time picker
        binding.etEndTime.setOnClickListener(v -> showTimePicker(false));
        binding.tilEndTime.setEndIconOnClickListener(v -> showTimePicker(false));
    }

    private void showDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.availability_date_hint)
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String formatted = sdf.format(new Date(selection));
                binding.etDate.setText(formatted);
                binding.tilDate.setError(null);
            }
        });

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(boolean isStart) {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(isStart ? 9 : 17)
                .setMinute(0)
                .setTitleText(isStart ? R.string.availability_start_time_hint : R.string.availability_end_time_hint)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            String time = String.format(Locale.US, "%02d:%02d", picker.getHour(), picker.getMinute());
            if (isStart) {
                binding.etStartTime.setText(time);
                binding.tilStartTime.setError(null);
            } else {
                binding.etEndTime.setText(time);
                binding.tilEndTime.setError(null);
            }
        });

        picker.show(getSupportFragmentManager(), isStart ? "START_TIME_PICKER" : "END_TIME_PICKER");
    }

    private void submitAvailability() {
        String date = binding.etDate.getText() != null ? binding.etDate.getText().toString().trim() : "";
        if (date.isEmpty()) {
            binding.tilDate.setError(getString(R.string.error_field_required));
            return;
        }
        binding.tilDate.setError(null);

        String startTime = binding.etStartTime.getText() != null ? binding.etStartTime.getText().toString().trim() : "";
        if (startTime.isEmpty()) {
            binding.tilStartTime.setError(getString(R.string.error_field_required));
            return;
        }
        binding.tilStartTime.setError(null);

        String endTime = binding.etEndTime.getText() != null ? binding.etEndTime.getText().toString().trim() : "";
        if (endTime.isEmpty()) {
            binding.tilEndTime.setError(getString(R.string.error_field_required));
            return;
        }
        binding.tilEndTime.setError(null);

        if (startTime.compareTo(endTime) >= 0) {
            binding.tilEndTime.setError(getString(R.string.error_time_order));
            return;
        }
        binding.tilEndTime.setError(null);

        String location = binding.etLocation.getText() != null ? binding.etLocation.getText().toString().trim() : null;
        if (location != null && location.isEmpty()) location = null;

        String notes = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : null;
        if (notes != null && notes.isEmpty()) notes = null;

        long guideId = SessionManager.getInstance().getUserId();
        CreateGuideAvailabilityRequest req = new CreateGuideAvailabilityRequest(
                guideId, date, startTime, endTime, location, notes
        );

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        ApiClient.getApiService().createGuideAvailability(req).enqueue(new ApiCallback<GuideAvailability>() {
            @Override
            public void onSuccess(GuideAvailability availability) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateAvailabilityActivity.this, R.string.availability_created_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmit.setEnabled(true);
                Toast.makeText(CreateAvailabilityActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
