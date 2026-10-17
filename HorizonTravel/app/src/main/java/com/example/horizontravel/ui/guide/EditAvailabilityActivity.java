package com.example.horizontravel.ui.guide;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.GuideAvailability;
import com.example.horizontravel.data.model.UpdateGuideAvailabilityRequest;
import com.example.horizontravel.databinding.ActivityEditAvailabilityBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EditAvailabilityActivity extends BaseActivity {

    public static final String EXTRA_AVAILABILITY_ID = "extra_availability_id";

    private ActivityEditAvailabilityBinding binding;
    private long availabilityId;
    private GuideAvailability currentAvailability;
    private boolean isLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.horizontravel.util.SessionManager session = com.example.horizontravel.util.SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.TOUR_GUIDE.equalsIgnoreCase(session.getRole())) {
            finish();
            return;
        }

        binding = ActivityEditAvailabilityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        availabilityId = getIntent().getLongExtra(EXTRA_AVAILABILITY_ID, -1);
        if (availabilityId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_availability_slo, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupPickers();
        setupListeners();
        loadAvailability();
    }

    private void setupPickers() {
        binding.etDate.setOnClickListener(v -> {
            if (!isLocked) showDatePicker();
        });
        binding.tilDate.setEndIconOnClickListener(v -> {
            if (!isLocked) showDatePicker();
        });

        binding.etStartTime.setOnClickListener(v -> {
            if (!isLocked) showTimePicker(true);
        });
        binding.tilStartTime.setEndIconOnClickListener(v -> {
            if (!isLocked) showTimePicker(true);
        });

        binding.etEndTime.setOnClickListener(v -> {
            if (!isLocked) showTimePicker(false);
        });
        binding.tilEndTime.setEndIconOnClickListener(v -> {
            if (!isLocked) showTimePicker(false);
        });
    }

    private void setupListeners() {
        binding.btnUpdate.setOnClickListener(v -> updateAvailability());

        binding.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm_delete_availability_title)
                    .setMessage(R.string.confirm_delete_availability_msg)
                    .setPositiveButton(R.string.yes, (dialog, which) -> cancelAvailability())
                    .setNegativeButton(R.string.no, null)
                    .show();
        });
    }

    private void loadAvailability() {
        long guideId = SessionManager.getInstance().getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getGuideAvailabilityByGuideId(guideId).enqueue(new ApiCallback<List<GuideAvailability>>() {
            @Override
            public void onSuccess(List<GuideAvailability> list) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);

                if (list != null) {
                    for (GuideAvailability a : list) {
                        if (a.getId() != null && a.getId() == availabilityId) {
                            currentAvailability = a;
                            displayAvailability(a);
                            return;
                        }
                    }
                }

                Toast.makeText(EditAvailabilityActivity.this, R.string.toast_slot_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditAvailabilityActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayAvailability(GuideAvailability a) {
        if (a == null) return;

        binding.etDate.setText(a.getDate() != null ? a.getDate() : "");
        binding.etStartTime.setText(a.getStartTime() != null ? a.getStartTime() : "");
        binding.etEndTime.setText(a.getEndTime() != null ? a.getEndTime() : "");
        binding.etLocation.setText(a.getLocation() != null ? a.getLocation() : "");
        binding.etNotes.setText(a.getNotes() != null ? a.getNotes() : "");

        String status = a.getStatus() != null ? a.getStatus().toUpperCase() : "OPEN";
        binding.tvCurrentStatus.setText(status);

        int statusColor;
        if (Constants.AvailabilityStatus.OPEN.equalsIgnoreCase(status)) {
            statusColor = getResources().getColor(R.color.teal_secondary);
        } else if (Constants.AvailabilityStatus.BOOKED.equalsIgnoreCase(status)) {
            statusColor = getResources().getColor(R.color.navy_primary);
        } else {
            statusColor = getResources().getColor(R.color.status_error);
        }
        binding.tvCurrentStatus.setTextColor(statusColor);

        boolean isBooked = Constants.AvailabilityStatus.BOOKED.equalsIgnoreCase(status);
        boolean isCancelled = Constants.AvailabilityStatus.CANCELLED.equalsIgnoreCase(status);
        isLocked = isBooked || isCancelled;

        if (isLocked) {
            binding.tvLockedNotice.setVisibility(View.VISIBLE);
            binding.etDate.setEnabled(false);
            binding.etStartTime.setEnabled(false);
            binding.etEndTime.setEnabled(false);
            binding.etLocation.setEnabled(false);
            binding.etNotes.setEnabled(false);
            binding.btnUpdate.setVisibility(View.GONE);

            // If already cancelled, hide cancel button as well
            if (isCancelled) {
                binding.btnDelete.setVisibility(View.GONE);
            } else {
                binding.btnDelete.setVisibility(View.VISIBLE);
            }
        } else {
            binding.tvLockedNotice.setVisibility(View.GONE);
            binding.etDate.setEnabled(true);
            binding.etStartTime.setEnabled(true);
            binding.etEndTime.setEnabled(true);
            binding.etLocation.setEnabled(true);
            binding.etNotes.setEnabled(true);
            binding.btnUpdate.setVisibility(View.VISIBLE);
            binding.btnDelete.setVisibility(View.VISIBLE);
        }
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

        picker.show(getSupportFragmentManager(), "EDIT_DATE_PICKER");
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

        picker.show(getSupportFragmentManager(), isStart ? "EDIT_START_TIME_PICKER" : "EDIT_END_TIME_PICKER");
    }

    private void updateAvailability() {
        if (isLocked) return;

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

        UpdateGuideAvailabilityRequest req = new UpdateGuideAvailabilityRequest();
        req.setDate(date);
        req.setStartTime(startTime);
        req.setEndTime(endTime);
        req.setLocation(location);
        req.setNotes(notes);
        req.setStatus(Constants.AvailabilityStatus.OPEN);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnUpdate.setEnabled(false);

        ApiClient.getApiService().updateGuideAvailability(availabilityId, req).enqueue(new ApiCallback<GuideAvailability>() {
            @Override
            public void onSuccess(GuideAvailability availability) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditAvailabilityActivity.this, R.string.availability_updated_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnUpdate.setEnabled(true);
                Toast.makeText(EditAvailabilityActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelAvailability() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnDelete.setEnabled(false);

        ApiClient.getApiService().deleteGuideAvailability(availabilityId).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(EditAvailabilityActivity.this, R.string.availability_deleted_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnDelete.setEnabled(true);
                Toast.makeText(EditAvailabilityActivity.this,
                        message != null ? message : "Unable to cancel slot",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
