package com.example.horizontravel.ui.provider;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Commission;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Payment;
import com.example.horizontravel.databinding.FragmentProviderLedgerBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.PdfDownloadHelper;
import com.example.horizontravel.util.SessionManager;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ProviderLedgerFragment extends BaseFragment<FragmentProviderLedgerBinding> {
    private CommissionAdapter commissionAdapter;
    private ProviderPaymentAdapter paymentAdapter;
    private final List<Commission> allCommissions = new ArrayList<>();
    private final List<Payment> allPayments = new ArrayList<>();
    private String selectedFilter;
    private String commissionFrom;
    private String commissionTo;
    private String paymentFrom;
    private String paymentTo;

    @Override
    protected FragmentProviderLedgerBinding inflateBinding(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container) {
        return FragmentProviderLedgerBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commissionAdapter = new CommissionAdapter();
        paymentAdapter = new ProviderPaymentAdapter();
        binding.rvCommissions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCommissions.setAdapter(commissionAdapter);
        binding.rvPayments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPayments.setAdapter(paymentAdapter);

        binding.tabLayoutLedger.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                boolean payments = tab.getPosition() == 1;
                binding.layoutPayments.setVisibility(payments ? View.VISIBLE : View.GONE);
                binding.layoutCommissions.setVisibility(payments ? View.GONE : View.VISIBLE);
                if (payments) {
                    loadPayments();
                } else {
                    loadCommissions();
                }
            }
            @Override public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            @Override public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });
        binding.tabLayoutLedger.getTabAt(0).select();

        binding.chipGroupCommissions.setOnCheckedChangeListener((group, checkedId) -> {
            selectedFilter = checkedId == R.id.chipCommPending ? Constants.SettlementStatus.PENDING
                    : checkedId == R.id.chipCommSettled ? Constants.SettlementStatus.SETTLED : null;
            applyCommissionFilter();
        });
        binding.swipeRefresh.setOnRefreshListener(this::loadData);
        binding.btnPickDateRange.setOnClickListener(v -> pickRange(true));
        binding.btnClearDate.setOnClickListener(v -> clearCommissionDates());
        binding.btnDownloadLedger.setOnClickListener(v -> downloadCommissions());
        binding.btnPickPaymentDateRange.setOnClickListener(v -> pickRange(false));
        binding.btnClearPaymentDate.setOnClickListener(v -> clearPaymentDates());
        binding.btnDownloadPayments.setOnClickListener(v -> downloadPayments());
        loadData();
    }

    private void loadData() {
        long providerId = SessionManager.getInstance().getUserId();
        if (providerId <= 0) return;
        binding.pbCommissions.setVisibility(View.VISIBLE);
        binding.pbPayments.setVisibility(View.VISIBLE);
        loadCommissions();
    }

    private void loadCommissions() {
        long providerId = SessionManager.getInstance().getUserId();
        if (providerId <= 0) return;
        binding.pbCommissions.setVisibility(View.VISIBLE);
        ApiClient.getApiService().getCommissionsForProvider(providerId).enqueue(new ApiCallback<List<Commission>>() {
            @Override public void onSuccess(List<Commission> data) {
                if (!isSafe()) return;
                binding.pbCommissions.setVisibility(View.GONE);
                allCommissions.clear();
                if (data != null) allCommissions.addAll(data);
                applyCommissionFilter();
                loadPayments();
            }
            @Override public void onError(String message, int statusCode) {
                if (isSafe()) {
                    binding.pbCommissions.setVisibility(View.GONE);
                    applyCommissionFilter();
                    loadPayments();
                }
            }
        });
    }

    private void loadPayments() {
        long providerId = SessionManager.getInstance().getUserId();
        if (providerId <= 0) return;
        binding.pbPayments.setVisibility(View.VISIBLE);
        loadProviderPayments(providerId, null, false);
    }

    private void loadProviderPayments(long providerId, String status, boolean pendingRequest) {
        ApiClient.getApiService().getPaymentsForProvider(providerId, paymentFrom, paymentTo, status)
                .enqueue(new ApiCallback<List<Payment>>() {
                    @Override public void onSuccess(List<Payment> data) {
                        if (!isSafe()) return;
                        binding.pbPayments.setVisibility(View.GONE);
                        if (!pendingRequest) {
                            allPayments.clear();
                        }
                        mergePayments(data);
                        binding.tvNoPayments.setText(R.string.provider_no_payments);
                        applyPaymentFilter();
                        if (!pendingRequest) {
                            if (allPayments.isEmpty()) {
                                loadBookingPaymentsFallback();
                            } else {
                                loadProviderPayments(providerId, Constants.PaymentTxStatus.PENDING, true);
                                binding.swipeRefresh.setRefreshing(false);
                            }
                        }
                    }
                    @Override public void onError(String message, int statusCode) {
                        if (isSafe()) {
                            binding.pbPayments.setVisibility(View.GONE);
                            if (!pendingRequest) {
                                loadBookingPaymentsFallback();
                            } else {
                                binding.tvNoPayments.setText(R.string.provider_no_payments);
                                applyPaymentFilter();
                                binding.swipeRefresh.setRefreshing(false);
                            }
                        }
                    }
                });
    }

    private void loadBookingPaymentsFallback() {
        List<Long> bookingIds = new ArrayList<>();
        for (Commission commission : allCommissions) {
            if (commission.getBookingId() != null && !bookingIds.contains(commission.getBookingId())) {
                bookingIds.add(commission.getBookingId());
            }
        }
        allPayments.clear();
        if (bookingIds.isEmpty()) {
            binding.pbPayments.setVisibility(View.GONE);
            binding.tvNoPayments.setText(R.string.provider_no_payments);
            applyPaymentFilter();
            binding.swipeRefresh.setRefreshing(false);
            return;
        }
        final int[] remaining = {bookingIds.size()};
        for (Long bookingId : bookingIds) {
            ApiClient.getApiService().getPaymentsForBooking(bookingId).enqueue(new ApiCallback<List<Payment>>() {
                @Override public void onSuccess(List<Payment> data) {
                    if (!isSafe()) return;
                    mergePayments(data);
                    finishBookingPaymentFallback(remaining);
                }

                @Override public void onError(String message, int statusCode) {
                    if (isSafe()) finishBookingPaymentFallback(remaining);
                }
            });
        }
    }

    private void finishBookingPaymentFallback(int[] remaining) {
        remaining[0]--;
        if (remaining[0] <= 0 && isSafe()) {
            binding.pbPayments.setVisibility(View.GONE);
            binding.tvNoPayments.setText(R.string.provider_no_payments);
            applyPaymentFilter();
            binding.swipeRefresh.setRefreshing(false);
        }
    }

    private void mergePayments(List<Payment> payments) {
        if (payments == null) return;
        for (Payment payment : payments) {
            if (payment == null) continue;
            boolean exists = false;
            for (Payment existing : allPayments) {
                if (payment.getId() != null && payment.getId().equals(existing.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) allPayments.add(payment);
        }
    }

    private void pickRange(boolean commissions) {
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.provider_pick_ledger_dates).build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection == null || selection.first == null || selection.second == null) return;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            String from = format.format(new Date(selection.first));
            String to = format.format(new Date(selection.second));
            if (commissions) {
                commissionFrom = from; commissionTo = to;
                binding.btnPickDateRange.setText(getString(R.string.date_range_format, from, to));
                binding.btnClearDate.setVisibility(View.VISIBLE);
                applyCommissionFilter();
            } else {
                paymentFrom = from; paymentTo = to;
                binding.btnPickPaymentDateRange.setText(getString(R.string.date_range_format, from, to));
                binding.btnClearPaymentDate.setVisibility(View.VISIBLE);
                loadData();
            }
        });
        picker.show(getChildFragmentManager(), commissions ? "COMMISSION_RANGE" : "PAYMENT_RANGE");
    }

    private void clearCommissionDates() {
        commissionFrom = null; commissionTo = null;
        binding.btnPickDateRange.setText(R.string.provider_pick_ledger_dates);
        binding.btnClearDate.setVisibility(View.GONE);
        applyCommissionFilter();
    }

    private void clearPaymentDates() {
        paymentFrom = null; paymentTo = null;
        binding.btnPickPaymentDateRange.setText(R.string.provider_pick_ledger_dates);
        binding.btnClearPaymentDate.setVisibility(View.GONE);
        loadData();
    }

    private void applyCommissionFilter() {
        List<Commission> filtered = new ArrayList<>();
        for (Commission item : allCommissions) {
            if ((selectedFilter == null || selectedFilter.equalsIgnoreCase(item.getSettlementStatus()))
                    && inRange(item.getCalculatedDate(), commissionFrom, commissionTo)) filtered.add(item);
        }
        commissionAdapter.setItems(filtered);
        binding.tvNoCommissions.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvCommissions.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void applyPaymentFilter() {
        List<Payment> filtered = new ArrayList<>();
        for (Payment item : allPayments) {
            if (inRange(item.getTransactionDate(), paymentFrom, paymentTo)) filtered.add(item);
        }
        paymentAdapter.setItems(filtered, allCommissions);
        binding.tvNoPayments.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvPayments.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private boolean inRange(String raw, String from, String to) {
        if (from == null && to == null) return true;
        if (raw == null || raw.length() < 10) return false;
        String day = raw.substring(0, 10);
        return (from == null || day.compareTo(from) >= 0) && (to == null || day.compareTo(to) <= 0);
    }

    private void downloadCommissions() {
        List<Commission> rows = new ArrayList<>();
        for (Commission item : allCommissions) {
            if ((selectedFilter == null || selectedFilter.equalsIgnoreCase(item.getSettlementStatus()))
                    && inRange(item.getCalculatedDate(), commissionFrom, commissionTo)) rows.add(item);
        }
        double total = 0;
        for (Commission item : rows) total += item.getAmount();
        downloadReport("Commissions_" + period(commissionFrom, commissionTo) + ".pdf",
                "COMMISSIONS REPORT", rows, total, true);
    }

    private void downloadPayments() {
        List<Payment> rows = new ArrayList<>();
        for (Payment item : allPayments) if (inRange(item.getTransactionDate(), paymentFrom, paymentTo)) rows.add(item);
        double total = 0;
        for (Payment item : rows) total += netPayment(item);
        downloadReport("Payments_" + period(paymentFrom, paymentTo) + ".pdf",
                "PAYMENTS REPORT", rows, total, false);
    }

    private void downloadReport(String fileName, String title, List<?> rows, double total, boolean commissions) {
        PdfDocument document = new PdfDocument();
        try {
            PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(595, 842, 1).create());
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.rgb(18, 61, 92));
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(22);
            canvas.drawText(title, 40, 48, paint);
            paint.setTypeface(android.graphics.Typeface.DEFAULT);
            paint.setTextSize(10);
            paint.setColor(Color.DKGRAY);
            String from = commissions ? commissionFrom : paymentFrom;
            String to = commissions ? commissionTo : paymentTo;
            canvas.drawText("Horizon All-in-One Travel Solutions", 40, 68, paint);
            canvas.drawText("Period: " + (from == null ? "All dates" : from) + " to " + (to == null ? "All dates" : to), 40, 84, paint);
            canvas.drawText("Generated: " + new SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.US).format(new Date()), 40, 100, paint);
            paint.setColor(Color.rgb(232, 241, 247));
            canvas.drawRect(40, 120, 555, 146, paint);
            paint.setColor(Color.rgb(18, 61, 92));
            paint.setTextSize(8);
            canvas.drawText("#", 46, 137, paint);
            canvas.drawText("Reference", 70, 137, paint);
            canvas.drawText("Type", 175, 137, paint);
            canvas.drawText("Date", 290, 137, paint);
            canvas.drawText("Status", 390, 137, paint);
            canvas.drawText("Net payout", 480, 137, paint);
            int y = 172;
            int index = 0;
            for (Object row : rows) {
                if (y > 690) break;
                String reference;
                String type;
                String date;
                String status;
                double amount;
                if (commissions) {
                    Commission item = (Commission) row;
                    reference = String.valueOf(item.getBookingId());
                    type = Formatters.capitalizeEnum(item.getServiceCategory());
                    date = Formatters.formatDate(item.getCalculatedDate());
                    status = item.getSettlementStatus();
                    amount = item.getAmount();
                } else {
                    Payment item = (Payment) row;
                    reference = "Payment #" + item.getId();
                    type = Formatters.capitalizeEnum(item.getMethod());
                    date = Formatters.formatDate(item.getTransactionDate());
                    status = item.getStatus();
                    amount = netPayment(item);
                }
                paint.setColor(Color.DKGRAY);
                paint.setTextSize(8);
                canvas.drawText(String.valueOf(++index), 46, y, paint);
                canvas.drawText(truncate(reference, 18), 70, y, paint);
                canvas.drawText(truncate(type, 18), 175, y, paint);
                canvas.drawText(truncate(date, 16), 290, y, paint);
                canvas.drawText(truncate(nonEmpty(status, "PENDING"), 14), 390, y, paint);
                canvas.drawText(Formatters.formatCurrency(amount), 490, y, paint);
                y += 25;
            }
            paint.setColor(Color.rgb(18, 61, 92));
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(10);
            canvas.drawText("TOTAL", 420, 735, paint);
            canvas.drawText(Formatters.formatCurrency(total), 490, 755, paint);
            paint.setTypeface(android.graphics.Typeface.DEFAULT);
            paint.setTextSize(8);
            canvas.drawText("This is a computer-generated report and does not require a signature.", 40, 800, paint);
            document.finishPage(page);
            PdfDownloadHelper.saveToDownloads(requireContext(), fileName, document);
            showToast("Report downloaded successfully");
        } catch (IOException exception) {
            showToast("Failed to download report");
        } finally {
            document.close();
        }
    }

    private String period(String from, String to) {
        return from != null && to != null ? from + "_to_" + to : "all";
    }

    private String truncate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    private String nonEmpty(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private double netPayment(Payment payment) {
        double commission = 0;
        for (Commission item : allCommissions) {
            if (sameBooking(payment, item)) commission += item.getAmount();
        }
        return Math.max(0, payment.getAmount() - commission);
    }

    private boolean sameBooking(Payment payment, Commission commission) {
        if (commission.getBookingId() == null) return false;
        return commission.getBookingId().equals(payment.getBookingId())
                || commission.getBookingId().equals(payment.getGuideBookingId());
    }
}
