package com.example.horizontravel.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.BookingsSummary;
import com.example.horizontravel.data.model.CommissionSummary;
import com.example.horizontravel.data.model.RevenueSummary;
import com.example.horizontravel.databinding.FragmentReportsBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.Formatters;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ReportsFragment extends BaseFragment<FragmentReportsBinding> {

    private String filterDateFrom = null;
    private String filterDateTo = null;

    @Override
    protected FragmentReportsBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentReportsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupDateRangePicker();

        binding.swipeRefresh.setOnRefreshListener(this::loadAllReports);

        loadAllReports();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllReports();
    }

    private void setupDateRangePicker() {
        binding.btnPickDateRange.setOnClickListener(v -> {
            if (isSingleClick()) {
                showDateRangePicker();
            }
        });

        binding.btnClearDate.setOnClickListener(v -> {
            filterDateFrom = null;
            filterDateTo = null;
            binding.btnPickDateRange.setText(R.string.btn_pick_date_range);
            binding.btnClearDate.setVisibility(View.GONE);
            loadAllReports();
        });
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.btn_pick_date_range)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                filterDateFrom = sdf.format(new Date(selection.first));
                filterDateTo = sdf.format(new Date(selection.second));

                binding.btnPickDateRange.setText(getString(R.string.date_range_format, filterDateFrom, filterDateTo));
                binding.btnClearDate.setVisibility(View.VISIBLE);

                loadAllReports();
            }
        });

        picker.show(getChildFragmentManager(), "REPORTS_DATE_RANGE");
    }

    private int reportsLoadedCount = 0;

    private void loadAllReports() {
        binding.progressBar.setVisibility(View.VISIBLE);
        reportsLoadedCount = 0;

        // 1. Revenue Summary
        ApiClient.getApiService().getRevenueSummary(filterDateFrom, filterDateTo).enqueue(new ApiCallback<RevenueSummary>() {
            @Override
            public void onSuccess(RevenueSummary summary) {
                if (!isSafe()) return;
                checkAllLoaded();
                if (summary != null) {
                    binding.tvRevenueTotal.setText(getString(R.string.report_total_revenue,
                            Formatters.formatCurrency(summary.getTotalRevenue())));
                    binding.tvRevenueCommission.setText(getString(R.string.report_total_commission,
                            Formatters.formatCurrency(summary.getTotalCommission())));
                    binding.tvRevenuePaymentCount.setText(getString(R.string.report_payment_count,
                            summary.getPaymentCount()));
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                checkAllLoaded();
                showError(message);
            }
        });

        // 2. Bookings Summary
        ApiClient.getApiService().getBookingsSummary(filterDateFrom, filterDateTo).enqueue(new ApiCallback<BookingsSummary>() {
            @Override
            public void onSuccess(BookingsSummary summary) {
                if (!isSafe()) return;
                checkAllLoaded();
                if (summary != null) {
                    binding.tvBookingsTotal.setText(getString(R.string.report_total_bookings,
                            summary.getTotalBookings()));
                    binding.tvBookingsPending.setText(String.valueOf(summary.getPendingCount()));
                    binding.tvBookingsConfirmed.setText(String.valueOf(summary.getConfirmedCount()));
                    binding.tvBookingsCompleted.setText(String.valueOf(summary.getCompletedCount()));
                    binding.tvBookingsCancelled.setText(String.valueOf(summary.getCancelledCount()));
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                checkAllLoaded();
            }
        });

        // 3. Commission Summary
        ApiClient.getApiService().getCommissionSummary(filterDateFrom, filterDateTo).enqueue(new ApiCallback<CommissionSummary>() {
            @Override
            public void onSuccess(CommissionSummary summary) {
                if (!isSafe()) return;
                checkAllLoaded();
                if (summary != null) {
                    binding.tvCommTotal.setText(getString(R.string.report_total_comm,
                            Formatters.formatCurrency(summary.getTotalCommission())));
                    binding.tvCommPending.setText(getString(R.string.report_pending_comm,
                            Formatters.formatCurrency(summary.getPendingCommission())));
                    binding.tvCommSettled.setText(getString(R.string.report_settled_comm,
                            Formatters.formatCurrency(summary.getSettledCommission())));
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                checkAllLoaded();
            }
        });
    }

    private void checkAllLoaded() {
        reportsLoadedCount++;
        if (reportsLoadedCount >= 3) {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
        }
    }
}
