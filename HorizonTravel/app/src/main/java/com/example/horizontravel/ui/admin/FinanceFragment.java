package com.example.horizontravel.ui.admin;

import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
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
import com.example.horizontravel.databinding.FragmentFinanceBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.PdfDownloadHelper;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

public class FinanceFragment extends BaseFragment<FragmentFinanceBinding> {

    private AdminPaymentAdapter paymentAdapter;
    private AdminCommissionAdapter commissionAdapter;

    private int currentSegment = 0; // 0 = Payments, 1 = Commissions
    private String filterDateFrom = null;
    private String filterDateTo = null;

    private String paymentStatus = null;
    private String paymentMethod = null;
    private String commissionStatus = null;
    private List<Payment> currentPayments = new ArrayList<>();
    private List<Commission> currentCommissions = new ArrayList<>();

    @Override
    protected FragmentFinanceBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFinanceBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapters();
        setupSegmentTabs();
        setupFilters();
        setupDateRangePicker();
        binding.btnDownloadFinanceReport.setOnClickListener(v -> {
            if (isSingleClick()) {
                downloadFinanceReport();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadActiveSegment);

        loadActiveSegment();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActiveSegment();
    }

    private void setupAdapters() {
        paymentAdapter = new AdminPaymentAdapter();
        commissionAdapter = new AdminCommissionAdapter(commission -> {
            if (isSingleClick() && commission != null) {
                confirmSettleCommission(commission);
            }
        });
        binding.rvFinance.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFinance.setAdapter(paymentAdapter);
    }

    private void setupSegmentTabs() {
        binding.tabLayoutSegment.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentSegment = tab.getPosition();
                if (currentSegment == 0) {
                    binding.layoutPaymentsFilters.setVisibility(View.VISIBLE);
                    binding.layoutCommissionsFilters.setVisibility(View.GONE);
                    binding.rvFinance.setAdapter(paymentAdapter);
                    binding.tvEmptyFinance.setText(R.string.admin_no_payments);
                } else {
                    binding.layoutPaymentsFilters.setVisibility(View.GONE);
                    binding.layoutCommissionsFilters.setVisibility(View.VISIBLE);
                    binding.rvFinance.setAdapter(commissionAdapter);
                    binding.tvEmptyFinance.setText(R.string.admin_no_commissions);
                }
                loadActiveSegment();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupFilters() {
        // Payment Status
        binding.chipGroupPaymentStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPayStatusSuccess) {
                paymentStatus = Constants.PaymentTxStatus.SUCCESS;
            } else if (checkedId == R.id.chipPayStatusPending) {
                paymentStatus = Constants.PaymentTxStatus.PENDING;
            } else if (checkedId == R.id.chipPayStatusFailed) {
                paymentStatus = Constants.PaymentTxStatus.FAILED;
            } else if (checkedId == R.id.chipPayStatusRefunded) {
                paymentStatus = Constants.PaymentTxStatus.REFUNDED;
            } else {
                paymentStatus = null;
            }
            if (currentSegment == 0) loadActiveSegment();
        });

        // Payment Method
        binding.chipGroupPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPayMethodCard) {
                paymentMethod = Constants.PaymentMethod.CARD;
            } else if (checkedId == R.id.chipPayMethodMobile) {
                paymentMethod = Constants.PaymentMethod.MOBILE_BANKING;
            } else if (checkedId == R.id.chipPayMethodCash) {
                paymentMethod = Constants.PaymentMethod.CASH;
            } else {
                paymentMethod = null;
            }
            if (currentSegment == 0) loadActiveSegment();
        });

        // Commission Settlement Status
        binding.chipGroupCommissionStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipCommStatusPending) {
                commissionStatus = Constants.SettlementStatus.PENDING;
            } else if (checkedId == R.id.chipCommStatusSettled) {
                commissionStatus = Constants.SettlementStatus.SETTLED;
            } else {
                commissionStatus = null;
            }
            if (currentSegment == 1) loadActiveSegment();
        });
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
            loadActiveSegment();
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

                loadActiveSegment();
            }
        });

        picker.show(getChildFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void loadActiveSegment() {
        if (currentSegment == 0) {
            loadPayments();
        } else {
            loadCommissions();
        }
    }

    private void loadPayments() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyFinance.setVisibility(View.GONE);

        ApiClient.getApiService().getAdminPayments(filterDateFrom, filterDateTo, paymentStatus, paymentMethod)
                .enqueue(new ApiCallback<List<Payment>>() {
                    @Override
                    public void onSuccess(List<Payment> payments) {
                        if (!isSafe() || currentSegment != 0) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        currentPayments = payments != null ? new ArrayList<>(payments) : new ArrayList<>();
                        paymentAdapter.setItems(payments);
                        boolean empty = payments == null || payments.isEmpty();
                        binding.layoutEmptyFinance.setVisibility(empty ? View.VISIBLE : View.GONE);
                        binding.rvFinance.setVisibility(empty ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onError(String message, int statusCode) {
                        if (!isSafe() || currentSegment != 0) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        showError(message);
                        binding.layoutEmptyFinance.setVisibility(paymentAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void loadCommissions() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyFinance.setVisibility(View.GONE);

        ApiClient.getApiService().getAdminCommissions(filterDateFrom, filterDateTo, commissionStatus)
                .enqueue(new ApiCallback<List<Commission>>() {
                    @Override
                    public void onSuccess(List<Commission> commissions) {
                        if (!isSafe() || currentSegment != 1) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        currentCommissions = commissions != null ? new ArrayList<>(commissions) : new ArrayList<>();
                        commissionAdapter.setItems(commissions);
                        boolean empty = commissions == null || commissions.isEmpty();
                        binding.layoutEmptyFinance.setVisibility(empty ? View.VISIBLE : View.GONE);
                        binding.rvFinance.setVisibility(empty ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onError(String message, int statusCode) {
                        if (!isSafe() || currentSegment != 1) return;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        showError(message);
                        binding.layoutEmptyFinance.setVisibility(commissionAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void confirmSettleCommission(Commission commission) {
        String amountFormatted = Formatters.formatCurrency(commission.getAmount());
        String provider = commission.getProviderName() != null ? commission.getProviderName() : "Provider";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_settle_title)
                .setMessage(getString(R.string.confirm_settle_msg, amountFormatted, provider))
                .setPositiveButton(R.string.btn_settle, (dialog, which) -> {
                    settleCommission(commission.getId());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void settleCommission(long commissionId) {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().settleCommission(commissionId).enqueue(new ApiCallback<Commission>() {
            @Override
            public void onSuccess(Commission response) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                showToast(getString(R.string.commission_settled_success));
                loadCommissions();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void downloadFinanceReport() {
        List<?> rows = currentSegment == 0 ? currentPayments : currentCommissions;
        String title = currentSegment == 0 ? "PAYMENTS REPORT" : "COMMISSIONS REPORT";
        String filePrefix = currentSegment == 0 ? "Payments_" : "Commissions_";
        PdfDocument document = new PdfDocument();
        try {
            PdfDocument.Page page = document.startPage(
                    new PdfDocument.PageInfo.Builder(595, 842, 1).create());
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            int navy = Color.rgb(18, 61, 92);
            paint.setColor(navy);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(22);
            canvas.drawText(title, 40, 48, paint);
            paint.setTypeface(android.graphics.Typeface.DEFAULT);
            paint.setTextSize(10);
            paint.setColor(Color.DKGRAY);
            canvas.drawText("Horizon All-in-One Travel Solutions", 40, 68, paint);
            canvas.drawText("Period: " + (filterDateFrom == null ? "All dates" : filterDateFrom)
                    + " to " + (filterDateTo == null ? "All dates" : filterDateTo), 40, 84, paint);
            canvas.drawText("Generated: " + new java.text.SimpleDateFormat(
                    "M/d/yyyy, h:mm:ss a", Locale.US).format(new Date()), 40, 100, paint);
            paint.setColor(Color.rgb(232, 241, 247));
            canvas.drawRect(40, 120, 555, 146, paint);
            paint.setColor(navy);
            paint.setTextSize(8);
            canvas.drawText("#", 46, 137, paint);
            canvas.drawText("Reference", 70, 137, paint);
            canvas.drawText("Type", 175, 137, paint);
            canvas.drawText("Date", 290, 137, paint);
            canvas.drawText("Status", 390, 137, paint);
            canvas.drawText("Amount", 490, 137, paint);

            double total = 0;
            int y = 172;
            int index = 0;
            for (Object row : rows) {
                if (y > 690) break;
                String reference;
                String type;
                String date;
                String status;
                double amount;
                if (currentSegment == 0) {
                    Payment payment = (Payment) row;
                    reference = payment.getId() == null ? "Payment" : "Payment #" + payment.getId();
                    type = Formatters.capitalizeEnum(payment.getMethod());
                    date = Formatters.formatDate(payment.getTransactionDate());
                    status = payment.getStatus();
                    amount = payment.getAmount();
                } else {
                    Commission commission = (Commission) row;
                    reference = commission.getBookingId() == null
                            ? "Commission" : "Booking #" + commission.getBookingId();
                    type = Formatters.capitalizeEnum(commission.getServiceCategory());
                    date = Formatters.formatDate(commission.getCalculatedDate());
                    status = commission.getSettlementStatus();
                    amount = commission.getAmount();
                }
                paint.setColor(Color.DKGRAY);
                paint.setTextSize(8);
                canvas.drawText(String.valueOf(++index), 46, y, paint);
                canvas.drawText(truncate(reference, 18), 70, y, paint);
                canvas.drawText(truncate(nonEmpty(type, "N/A"), 18), 175, y, paint);
                canvas.drawText(truncate(nonEmpty(date, "N/A"), 16), 290, y, paint);
                canvas.drawText(truncate(nonEmpty(status, "PENDING"), 14), 390, y, paint);
                canvas.drawText(Formatters.formatCurrency(amount), 490, y, paint);
                total += amount;
                y += 25;
            }
            paint.setColor(navy);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(10);
            canvas.drawText("TOTAL", 420, 735, paint);
            canvas.drawText(Formatters.formatCurrency(total), 490, 755, paint);
            paint.setTypeface(android.graphics.Typeface.DEFAULT);
            paint.setTextSize(8);
            canvas.drawText("This is a computer-generated report and does not require a signature.",
                    40, 800, paint);
            document.finishPage(page);
            String suffix = filterDateFrom != null && filterDateTo != null
                    ? filterDateFrom + "_to_" + filterDateTo : "all";
            PdfDownloadHelper.saveToDownloads(requireContext(), filePrefix + suffix + ".pdf", document);
            showToast("Report downloaded successfully");
        } catch (IOException exception) {
            showToast("Failed to download report");
        } finally {
            document.close();
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

    private String nonEmpty(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
