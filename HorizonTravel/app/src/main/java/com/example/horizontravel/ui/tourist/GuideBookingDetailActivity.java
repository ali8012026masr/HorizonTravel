package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import java.io.IOException;

import androidx.appcompat.app.AlertDialog;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.GuideBooking;
import com.example.horizontravel.data.model.Payment;
import com.example.horizontravel.data.model.PaymentRequest;
import com.example.horizontravel.data.model.UpdateGuideBookingStatusRequest;
import com.example.horizontravel.databinding.ActivityGuideBookingDetailBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.PdfDownloadHelper;
import com.example.horizontravel.util.SessionManager;

import java.util.List;

public class GuideBookingDetailActivity extends BaseActivity {

    public static final String EXTRA_GUIDE_BOOKING_ID = "extra_guide_booking_id";

    private ActivityGuideBookingDetailBinding binding;
    private long guideBookingId;
    private GuideBooking currentBooking;
    private Guide currentGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideBookingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        guideBookingId = getIntent().getLongExtra(EXTRA_GUIDE_BOOKING_ID, -1);
        if (guideBookingId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_guide_booking, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setupListeners();
        loadGuideBooking();
    }

    private void setupListeners() {
        binding.btnCancelBooking.setOnClickListener(v -> showCancelConfirmDialog());

        binding.btnPayNow.setOnClickListener(v -> showPayGuideDialog());

        binding.btnLiveLocation.setOnClickListener(v -> openLiveLocation());

        binding.btnRateGuide.setOnClickListener(v -> openRateGuide());

        if (binding.btnDownloadInvoice != null) {
            binding.btnDownloadInvoice.setOnClickListener(v -> {
                if (isSingleClick() && currentBooking != null) {
                    generatePdf();
                }
            });
        }
    }

    private void loadGuideBooking() {
        long userId = SessionManager.getInstance().getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getGuideBookingsForTourist(userId).enqueue(new ApiCallback<List<GuideBooking>>() {
            @Override
            public void onSuccess(List<GuideBooking> bookings) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);

                if (bookings != null) {
                    for (GuideBooking b : bookings) {
                        if (b != null && b.getId() != null && b.getId() == guideBookingId) {
                            currentBooking = b;
                            displayBooking(b);
                            if (b.getTourGuideId() != null) {
                                loadGuide(b.getTourGuideId());
                            }
                            return;
                        }
                    }
                }
                Toast.makeText(GuideBookingDetailActivity.this, R.string.toast_guide_booking_not_found, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GuideBookingDetailActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBooking(GuideBooking booking) {
        binding.tvGuideBookingHeader.setText(getString(R.string.guide_hire_id_format, booking.getId()));

        String status = booking.getStatus() != null ? booking.getStatus() : "UNKNOWN";
        binding.tvGuideBookingStatus.setText(status);

        String dateStr = booking.getScheduleDate() != null ? Formatters.formatDate(booking.getScheduleDate()) : "";
        binding.tvScheduleDate.setText(getString(R.string.guide_hire_schedule_date_label, dateStr));

        boolean isNegotiated = Boolean.TRUE.equals(booking.getNegotiated());
        binding.tvNegotiatedBadge.setText(isNegotiated
                ? getString(R.string.guide_negotiated_tag)
                : getString(R.string.guide_standard_tag));

        boolean paid = Boolean.TRUE.equals(booking.getPaymentReceived());
        binding.tvPaymentStatus.setText(getString(R.string.booking_payment_label,
                paid ? getString(R.string.payment_received_label) : getString(R.string.payment_pending_label)));

        binding.tvAgreedPrice.setText(Formatters.formatCurrency(booking.getAgreedPrice()));

        // Buttons visibility
        boolean isRequested = Constants.GuideBookingStatus.REQUESTED.equalsIgnoreCase(status);
        boolean isAccepted = Constants.GuideBookingStatus.ACCEPTED.equalsIgnoreCase(status);
        boolean isCompleted = Constants.GuideBookingStatus.COMPLETED.equalsIgnoreCase(status);
        boolean canCancel = isRequested || isAccepted;

        binding.btnCancelBooking.setVisibility(canCancel ? View.VISIBLE : View.GONE);
        binding.btnLiveLocation.setVisibility(isAccepted ? View.VISIBLE : View.GONE);
        binding.btnPayNow.setVisibility(isAccepted && !paid ? View.VISIBLE : View.GONE);
        binding.btnRateGuide.setVisibility(isCompleted ? View.VISIBLE : View.GONE);

        if (binding.btnDownloadInvoice != null) {
            binding.btnDownloadInvoice.setVisibility(paid ? View.VISIBLE : View.GONE);
        }
    }

    private void loadGuide(long guideId) {
        ApiClient.getApiService().getGuideById(guideId).enqueue(new ApiCallback<Guide>() {
            @Override
            public void onSuccess(Guide guide) {
                if (isFinishing() || isDestroyed()) return;
                currentGuide = guide;
                if (guide != null) {
                    if (guide.getFullName() != null) {
                        binding.tvGuideName.setText(guide.getFullName());
                    }
                    if (guide.getLocation() != null && !guide.getLocation().trim().isEmpty()) {
                        binding.tvGuideLocation.setText(getString(R.string.guide_location_label) + ": " + guide.getLocation());
                        binding.tvGuideLocation.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvGuideLocation.setVisibility(View.GONE);
                    }

                    if (guide.getRatingAvg() != null && guide.getRatingAvg() > 0) {
                        int count = guide.getRatingCount() != null ? guide.getRatingCount() : 0;
                        binding.tvGuideRating.setText(String.format(java.util.Locale.US, "★ %.1f (%d reviews)", guide.getRatingAvg(), count));
                        binding.tvGuideRating.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvGuideRating.setVisibility(View.GONE);
                    }

                    if (guide.getExperienceYears() != null && guide.getExperienceYears() > 0) {
                        binding.tvGuideExperience.setText(getString(R.string.guide_experience_years, guide.getExperienceYears()));
                        binding.tvGuideExperience.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvGuideExperience.setVisibility(View.GONE);
                    }

                    if (guide.getLanguages() != null && !guide.getLanguages().isEmpty()) {
                        binding.tvGuideLanguages.setText(getString(R.string.guide_languages_label) + ": " + String.join(", ", guide.getLanguages()));
                        binding.tvGuideLanguages.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvGuideLanguages.setVisibility(View.GONE);
                    }

                    if (guide.getBio() != null && !guide.getBio().trim().isEmpty()) {
                        binding.tvGuideBio.setText(guide.getBio());
                        binding.tvGuideBio.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvGuideBio.setVisibility(View.GONE);
                    }

                    if (guide.getPhone() != null && !guide.getPhone().trim().isEmpty()) {
                        binding.tvGuidePhone.setText(getString(R.string.guide_contact_phone, guide.getPhone()));
                        binding.tvGuidePhone.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvGuidePhone.setVisibility(View.GONE);
                    }

                    if (guide.getEmail() != null && !guide.getEmail().trim().isEmpty()) {
                        binding.tvGuideEmail.setText(getString(R.string.guide_contact_email, guide.getEmail()));
                        binding.tvGuideEmail.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvGuideEmail.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                // Non-fatal: basic booking information is still visible
            }
        });
    }

    private void showCancelConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_cancel_guide_booking_title)
                .setMessage(R.string.confirm_cancel_guide_booking_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> cancelGuideBooking())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void cancelGuideBooking() {
        binding.progressBar.setVisibility(View.VISIBLE);
        UpdateGuideBookingStatusRequest statusRequest = new UpdateGuideBookingStatusRequest(Constants.GuideBookingStatus.DECLINED);

        ApiClient.getApiService().updateGuideBookingStatus(guideBookingId, statusRequest).enqueue(new ApiCallback<GuideBooking>() {
            @Override
            public void onSuccess(GuideBooking booking) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GuideBookingDetailActivity.this, R.string.guide_booking_cancelled_success, Toast.LENGTH_SHORT).show();
                loadGuideBooking();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GuideBookingDetailActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPayGuideDialog() {
        if (currentBooking == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_pay_guide_fee)
                .setMessage(String.format(java.util.Locale.US, "Confirm payment of %s for this guide booking?",
                        Formatters.formatCurrency(currentBooking.getAgreedPrice())))
                .setPositiveButton("Pay via Card", (dialog, which) -> {
                    processGuidePayment(Constants.PaymentMethod.CARD);
                })
                .setNeutralButton("Pay via Mobile Banking", (dialog, which) -> {
                    processGuidePayment(Constants.PaymentMethod.MOBILE_BANKING);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void processGuidePayment(String method) {
        if (currentBooking == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        PaymentRequest paymentRequest = new PaymentRequest(
                null,
                currentBooking.getId(),
                currentBooking.getAgreedPrice(),
                method
        );

        ApiClient.getApiService().createPayment(paymentRequest).enqueue(new ApiCallback<Payment>() {
            @Override
            public void onSuccess(Payment payment) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GuideBookingDetailActivity.this, R.string.toast_payment_processed_succes, Toast.LENGTH_SHORT).show();
                loadGuideBooking();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GuideBookingDetailActivity.this,
                        message != null ? message : "Payment failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openLiveLocation() {
        if (currentBooking != null && currentBooking.getTourGuideId() != null) {
            Intent intent = new Intent(this, GuideLocationActivity.class);
            intent.putExtra(GuideLocationActivity.EXTRA_GUIDE_ID, currentBooking.getTourGuideId());
            startActivity(intent);
        }
    }

    private void openRateGuide() {
        if (currentBooking != null && currentBooking.getId() != null) {
            Intent intent = new Intent(this, RateGuideActivity.class);
            intent.putExtra(RateGuideActivity.EXTRA_GUIDE_BOOKING_ID, currentBooking.getId());
            startActivity(intent);
        }
    }

    private void generatePdf() {
        if (currentBooking == null) return;
        
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        
        int y = 50;
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText("Horizon Travel Guide Invoice", 50, y, paint);
        y += 40;
        
        paint.setTextSize(16);
        paint.setFakeBoldText(false);
        canvas.drawText("Booking ID: " + currentBooking.getId(), 50, y, paint);
        y += 30;
        canvas.drawText("Status: " + currentBooking.getStatus(), 50, y, paint);
        y += 30;
        
        if (currentGuide != null && currentGuide.getFullName() != null) {
            canvas.drawText("Guide: " + currentGuide.getFullName(), 50, y, paint);
            y += 30;
        }

        if (currentBooking.getScheduleDate() != null) {
            canvas.drawText("Schedule Date: " + currentBooking.getScheduleDate(), 50, y, paint);
            y += 30;
        }
        
        canvas.drawText("Amount: " + Formatters.formatCurrency(currentBooking.getAgreedPrice()), 50, y, paint);
        y += 30;
        boolean paid = Boolean.TRUE.equals(currentBooking.getPaymentReceived());
        canvas.drawText("Payment Status: " + (paid ? "PAID" : "PENDING"), 50, y, paint);

        document.finishPage(page);

        try {
            PdfDownloadHelper.saveToDownloads(this, "Guide_Invoice_" + currentBooking.getId() + ".pdf", document);
            Toast.makeText(this, "Invoice downloaded successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to download invoice", Toast.LENGTH_SHORT).show();
        }
        document.close();
    }
}
