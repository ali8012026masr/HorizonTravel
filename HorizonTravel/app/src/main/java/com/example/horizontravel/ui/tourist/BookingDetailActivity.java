package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Booking;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Payment;
import com.example.horizontravel.data.model.PaymentRequest;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.data.model.StatusRequest;
import com.example.horizontravel.databinding.ActivityBookingDetailBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.PdfDownloadHelper;
import com.example.horizontravel.util.SessionManager;

import java.util.List;

public class BookingDetailActivity extends BaseActivity {

    public static final String EXTRA_BOOKING_ID = "extra_booking_id";

    private ActivityBookingDetailBinding binding;
    private long bookingId;
    private Booking currentBooking;
    private ServiceSlot currentSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookingId = getIntent().getLongExtra(EXTRA_BOOKING_ID, -1);
        if (bookingId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_booking, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setupListeners();
        loadBooking();
    }

    private void setupListeners() {
        binding.btnCancelBooking.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm_cancel_booking_title)
                    .setMessage(R.string.confirm_cancel_booking_msg)
                    .setPositiveButton(R.string.yes, (dialog, which) -> cancelBooking())
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        binding.btnPayNow.setOnClickListener(v -> {
            showPaymentDialog();
        });

        binding.btnRateService.setOnClickListener(v -> {
            if (currentBooking != null) {
                Intent intent = new Intent(this, RateServiceActivity.class);
                intent.putExtra(RateServiceActivity.EXTRA_BOOKING_ID, currentBooking.getId());
                if (currentSlot != null && currentSlot.getProviderId() != null) {
                    intent.putExtra(RateServiceActivity.EXTRA_PROVIDER_ID, currentSlot.getProviderId());
                }
                startActivity(intent);
            }
        });

        if (binding.btnDownloadInvoice != null) {
            binding.btnDownloadInvoice.setOnClickListener(v -> {
                if (isSingleClick() && currentBooking != null) {
                    generatePdf();
                }
            });
        }
    }

    private void loadBooking() {
        long userId = SessionManager.getInstance().getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getBookingsForTourist(userId).enqueue(new ApiCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);

                if (bookings != null) {
                    for (Booking b : bookings) {
                        if (b.getId() != null && b.getId() == bookingId) {
                            currentBooking = b;
                            displayBooking(b);
                            if (b.getServiceSlotId() != null) {
                                loadSlot(b.getServiceSlotId());
                            }
                            loadPayments();
                            return;
                        }
                    }
                }
                Toast.makeText(BookingDetailActivity.this, R.string.toast_booking_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(BookingDetailActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBooking(Booking booking) {
        binding.tvBookingHeader.setText(getString(R.string.booking_id_prefix, booking.getId()));
        binding.tvBookingStatus.setText(booking.getStatus() != null ? booking.getStatus() : "UNKNOWN");
        binding.tvTotalAmount.setText(Formatters.formatCurrency(booking.getTotalAmount()));

        String payment = booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "UNPAID";
        binding.tvPaymentStatus.setText(getString(R.string.booking_payment_label, payment));

        boolean isCompleted = Constants.BookingStatus.COMPLETED.equalsIgnoreCase(booking.getStatus());
        boolean isCancelled = Constants.BookingStatus.CANCELLED.equalsIgnoreCase(booking.getStatus());
        boolean isPaid = Constants.PaymentStatus.PAID.equalsIgnoreCase(booking.getPaymentStatus());

        binding.btnCancelBooking.setVisibility(!isCancelled && !isCompleted ? View.VISIBLE : View.GONE);
        binding.btnPayNow.setVisibility(!isPaid && !isCancelled ? View.VISIBLE : View.GONE);
        binding.btnRateService.setVisibility(isCompleted ? View.VISIBLE : View.GONE);

        if (binding.btnDownloadInvoice != null) {
            binding.btnDownloadInvoice.setVisibility(isPaid ? View.VISIBLE : View.GONE);
        }
    }

    private void loadSlot(long slotId) {
        ApiClient.getApiService().getSlotById(slotId).enqueue(new ApiCallback<ServiceSlot>() {
            @Override
            public void onSuccess(ServiceSlot slot) {
                if (isFinishing() || isDestroyed()) return;
                currentSlot = slot;
                if (slot != null) {
                    String title = slot.getLocationName();
                    if (title == null || title.trim().isEmpty()) {
                        if (slot.getOrigin() != null && slot.getDestination() != null) {
                            title = slot.getOrigin() + " → " + slot.getDestination();
                        } else {
                            title = slot.getCategory() != null ? slot.getCategory() : "Service Slot";
                        }
                    }
                    binding.tvSlotTitle.setText(title);

                    String dateStr = "";
                    if (slot.getStartDateTime() != null) {
                        dateStr = "Start: " + Formatters.formatDateTime(slot.getStartDateTime());
                    }
                    if (slot.getEndDateTime() != null) {
                        dateStr += "  |  End: " + Formatters.formatDateTime(slot.getEndDateTime());
                    }
                    binding.tvSlotDates.setText(dateStr);
                }
            }

            @Override
            public void onError(String message, int statusCode) {}
        });
    }

    private void loadPayments() {
        ApiClient.getApiService().getPaymentsForBooking(bookingId).enqueue(new ApiCallback<List<Payment>>() {
            @Override
            public void onSuccess(List<Payment> payments) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutPaymentsContainer.removeAllViews();

                if (payments == null || payments.isEmpty()) {
                    binding.tvNoPayments.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoPayments.setVisibility(View.GONE);
                    LayoutInflater inflater = LayoutInflater.from(BookingDetailActivity.this);
                    for (Payment p : payments) {
                        com.example.horizontravel.databinding.ItemTwoLineBinding itemBinding =
                                com.example.horizontravel.databinding.ItemTwoLineBinding.inflate(inflater, binding.layoutPaymentsContainer, false);

                        itemBinding.tvText1.setText(String.format(java.util.Locale.US, "Transaction #%d  •  %s",
                                p.getId() != null ? p.getId() : 0,
                                Formatters.formatCurrency(p.getAmount())));
                        itemBinding.tvText1.setTextColor(getResources().getColor(R.color.navy_primary));

                        String method = p.getMethod() != null ? p.getMethod() : "CARD";
                        String status = p.getStatus() != null ? p.getStatus() : "SUCCESS";
                        String date = p.getTransactionDate() != null ? Formatters.formatDateTime(p.getTransactionDate()) : "";
                        itemBinding.tvText2.setText(String.format(java.util.Locale.US, "%s  |  Status: %s  |  %s", method, status, date));
                        itemBinding.tvText2.setTextColor(getResources().getColor(R.color.on_surface_variant));

                        binding.layoutPaymentsContainer.addView(itemBinding.getRoot());
                    }
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.tvNoPayments.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cancelBooking() {
        binding.progressBar.setVisibility(View.VISIBLE);
        StatusRequest statusRequest = new StatusRequest(Constants.BookingStatus.CANCELLED);

        ApiClient.getApiService().updateBookingStatus(bookingId, statusRequest).enqueue(new ApiCallback<Booking>() {
            @Override
            public void onSuccess(Booking booking) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(BookingDetailActivity.this, R.string.booking_cancelled_success, Toast.LENGTH_SHORT).show();
                loadBooking();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(BookingDetailActivity.this,
                        message != null ? message : "Unable to cancel booking",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPaymentDialog() {
        if (currentBooking == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_complete_payment)
                .setMessage(String.format(java.util.Locale.US, "Pay %s for Booking #%d?",
                        Formatters.formatCurrency(currentBooking.getTotalAmount()), bookingId))
                .setPositiveButton("Pay via Card", (dialog, which) -> {
                    submitPayment(Constants.PaymentMethod.CARD);
                })
                .setNeutralButton("Pay via Mobile Banking", (dialog, which) -> {
                    submitPayment(Constants.PaymentMethod.MOBILE_BANKING);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitPayment(String method) {
        if (currentBooking == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        PaymentRequest paymentRequest = new PaymentRequest(
                currentBooking.getId(),
                null,
                currentBooking.getTotalAmount(),
                method
        );

        ApiClient.getApiService().createPayment(paymentRequest).enqueue(new ApiCallback<Payment>() {
            @Override
            public void onSuccess(Payment payment) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(BookingDetailActivity.this, R.string.toast_payment_successful, Toast.LENGTH_SHORT).show();
                loadBooking();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(BookingDetailActivity.this,
                        message != null ? message : "Payment failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePdf() {
        if (currentBooking == null) return;

        PdfDocument document = new PdfDocument();
        PdfDocument.Page page = document.startPage(
                new PdfDocument.PageInfo.Builder(595, 842, 1).create());
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int navy = Color.rgb(18, 61, 92);
        int lightBlue = Color.rgb(232, 241, 247);
        int left = 40;
        int right = 555;
        int y = 42;

        paint.setColor(navy);
        paint.setTextSize(22);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        canvas.drawText("INVOICE", left, y, paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setTextSize(10);
        paint.setColor(Color.DKGRAY);
        canvas.drawText("Horizon All-in-One Travel Solutions", left, y + 20, paint);
        canvas.drawText("Generated: " + new SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.US).format(new Date()),
                left, y + 34, paint);

        paint.setColor(navy);
        paint.setTextSize(9);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        canvas.drawText("Invoice No.", 405, y + 4, paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setTextSize(11);
        canvas.drawText(invoiceNumber(), 405, y + 20, paint);

        y += 72;
        paint.setStrokeWidth(1.5f);
        canvas.drawLine(left, y, right, y, paint);
        y += 25;
        paint.setTextSize(10);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        canvas.drawText("BILLED TO", left, y, paint);
        canvas.drawText("INVOICE DETAILS", 315, y, paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setColor(Color.DKGRAY);
        canvas.drawText(nonEmpty(SessionManager.getInstance().getFullName(), "Tourist"), left, y + 19, paint);
        canvas.drawText(SessionManager.getInstance().getEmail(), left, y + 34, paint);
        canvas.drawText("Issue date: " + Formatters.formatDate(currentBooking.getBookingDate()), 315, y + 19, paint);
        canvas.drawText("Payment status: " + nonEmpty(currentBooking.getPaymentStatus(), "PENDING"), 315, y + 34, paint);
        y += 72;

        paint.setColor(lightBlue);
        canvas.drawRect(left, y - 16, right, y + 10, paint);
        paint.setColor(navy);
        paint.setTextSize(8);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        canvas.drawText("#", 46, y, paint);
        canvas.drawText("Service", 72, y, paint);
        canvas.drawText("Category", 315, y, paint);
        canvas.drawText("Booked On", 395, y, paint);
        canvas.drawText("Status", 465, y, paint);
        canvas.drawText("Amount", 520, y, paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);

        y += 28;
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(8);
        canvas.drawText("1", 46, y, paint);
        canvas.drawText(truncate(serviceName(), 34), 72, y, paint);
        canvas.drawText(truncate(currentSlot != null
                ? Formatters.capitalizeEnum(currentSlot.getCategory()) : "Service", 13), 315, y, paint);
        canvas.drawText(Formatters.formatDate(currentBooking.getBookingDate()), 395, y, paint);
        canvas.drawText(nonEmpty(currentBooking.getStatus(), "CONFIRMED"), 465, y, paint);
        canvas.drawText(Formatters.formatCurrency(currentBooking.getTotalAmount()), 520, y, paint);
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(left, y + 12, right, y + 12, paint);
        y += 55;

        paint.setColor(navy);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD));
        paint.setTextSize(10);
        canvas.drawText("TOTAL EXPENSES", 390, y, paint);
        paint.setTextSize(14);
        canvas.drawText(Formatters.formatCurrency(currentBooking.getTotalAmount()), 475, y + 22, paint);
        paint.setTypeface(android.graphics.Typeface.DEFAULT);
        paint.setColor(Color.rgb(90, 103, 112));
        paint.setTextSize(8);
        canvas.drawText("This is a computer-generated invoice and does not require a signature.", left, 760, paint);
        canvas.drawText("Thank you for travelling with Horizon.", left, 774, paint);
        canvas.drawText("www.horizon.app  |  support@horizon.app  |  +880-1700-000000", left, 808, paint);
        canvas.drawText("Page 1 of 1", 485, 808, paint);

        document.finishPage(page);

        try {
            PdfDownloadHelper.saveToDownloads(this, "Invoice_" + currentBooking.getId() + ".pdf", document);
            Toast.makeText(this, "Invoice downloaded successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to download invoice", Toast.LENGTH_SHORT).show();
        }
        document.close();
    }

    private String invoiceNumber() {
        return "INV-" + new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date())
                + "-" + currentBooking.getId();
    }

    private String serviceName() {
        if (currentSlot == null) return "Service booking";
        String title = currentSlot.getLocationName();
        if (title == null || title.trim().isEmpty()) {
            if (currentSlot.getOrigin() != null && currentSlot.getDestination() != null) {
                title = currentSlot.getOrigin() + " -> " + currentSlot.getDestination();
            } else {
                title = currentSlot.getCategory();
            }
        }
        return nonEmpty(title, "Service booking");
    }

    private String nonEmpty(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return nonEmpty(value, "");
        return value.substring(0, maxLength - 3) + "...";
    }
}
