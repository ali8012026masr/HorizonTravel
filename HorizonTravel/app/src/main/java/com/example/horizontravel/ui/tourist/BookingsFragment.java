package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Booking;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.GuideBooking;
import com.example.horizontravel.data.model.Payment;
import com.example.horizontravel.data.model.PaymentRequest;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.data.model.UpdateGuideBookingStatusRequest;
import com.example.horizontravel.databinding.FragmentBookingsBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.SessionManager;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.PdfDownloadHelper;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingsFragment extends BaseFragment<FragmentBookingsBinding> {

    private BookingAdapter bookingAdapter;
    private GuideBookingAdapter guideBookingAdapter;
    private int currentTab = 0; // 0 = Services, 1 = Guides
    private final List<Booking> serviceBookings = new ArrayList<>();
    private final List<GuideBooking> guideBookings = new ArrayList<>();

    @Override
    protected FragmentBookingsBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentBookingsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAdapters();
        setupListeners();
        binding.etSearchBookings.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        binding.btnDownloadBookingsReport.setOnClickListener(v -> {
            if (isSingleClick()) {
                downloadBookingsReport();
            }
        });
        loadCurrentTab();
    }

    private void downloadBookingsReport() {
        if (currentTab == 0 && serviceBookings.isEmpty()
                || currentTab == 1 && guideBookings.isEmpty()) {
            showToast("No bookings available for the report");
            return;
        }

        List<String[]> rows = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();
        if (currentTab == 0) {
            for (Booking booking : serviceBookings) {
                rows.add(new String[] {
                        "#" + value(booking.getId()),
                        "Service #" + value(booking.getServiceSlotId()),
                        Formatters.formatDateTime(booking.getBookingDate()),
                        value(booking.getStatus()),
                        value(booking.getPaymentStatus())
                });
                amounts.add(booking.getTotalAmount());
            }
        } else {
            for (GuideBooking booking : guideBookings) {
                rows.add(new String[] {
                        "#" + value(booking.getId()),
                        "Guide #" + value(booking.getTourGuideId()),
                        Formatters.formatDate(booking.getScheduleDate()),
                        value(booking.getStatus()),
                        Boolean.TRUE.equals(booking.getPaymentReceived()) ? "PAID" : "UNPAID"
                });
                amounts.add(booking.getAgreedPrice());
            }
        }

        PdfDocument document = new PdfDocument();
        try {
            final int rowsPerPage = 24;
            double total = 0;
            for (Double amount : amounts) {
                total += amount != null ? amount : 0;
            }
            int pageCount = (rows.size() + rowsPerPage - 1) / rowsPerPage;
            for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
                PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(
                        595, 842, pageNumber + 1).create());
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                drawReportHeader(canvas, paint, pageNumber + 1, pageCount);
                int start = pageNumber * rowsPerPage;
                int end = Math.min(start + rowsPerPage, rows.size());
                int y = 165;
                for (int index = start; index < end; index++) {
                    String[] row = rows.get(index);
                    paint.setColor(Color.DKGRAY);
                    paint.setTypeface(Typeface.DEFAULT);
                    paint.setTextSize(8);
                    canvas.drawText(String.valueOf(index + 1), 42, y, paint);
                    canvas.drawText(truncate(row[0], 14), 62, y, paint);
                    canvas.drawText(truncate(row[1], 19), 140, y, paint);
                    canvas.drawText(truncate(row[2], 20), 245, y, paint);
                    canvas.drawText(truncate(row[3], 13), 365, y, paint);
                    canvas.drawText(truncate(row[4], 13), 440, y, paint);
                    canvas.drawText(Formatters.formatCurrency(amounts.get(index)), 505, y, paint);
                    y += 26;
                }
                if (pageNumber == pageCount - 1) {
                    paint.setColor(Color.rgb(18, 61, 92));
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    paint.setTextSize(10);
                    canvas.drawText("TOTAL BOOKINGS: " + rows.size(), 365, 735, paint);
                    canvas.drawText("TOTAL AMOUNT: " + Formatters.formatCurrency(total), 365, 755, paint);
                    paint.setTypeface(Typeface.DEFAULT);
                    paint.setTextSize(8);
                    canvas.drawText("This is a computer-generated report and does not require a signature.",
                            40, 800, paint);
                }
                document.finishPage(page);
            }

            String type = currentTab == 0 ? "Services" : "Guide_Hires";
            String fileName = "Bookings_" + type + "_"
                    + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".pdf";
            PdfDownloadHelper.saveToDownloads(requireContext(), fileName, document);
            showToast("Bookings report downloaded successfully");
        } catch (IOException exception) {
            showToast("Failed to download bookings report");
        } finally {
            document.close();
        }
    }

    private void drawReportHeader(Canvas canvas, Paint paint, int page, int pageCount) {
        int navy = Color.rgb(18, 61, 92);
        paint.setColor(navy);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(22);
        canvas.drawText(currentTab == 0 ? "SERVICE BOOKINGS REPORT" : "GUIDE HIRES REPORT", 40, 48, paint);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(10);
        paint.setColor(Color.DKGRAY);
        canvas.drawText("Horizon All-in-One Travel Solutions", 40, 68, paint);
        canvas.drawText("Generated: " + new SimpleDateFormat("M/d/yyyy, h:mm:ss a",
                Locale.US).format(new Date()), 40, 84, paint);
        canvas.drawText("Page " + page + " of " + pageCount + " | Includes all statuses", 40, 100, paint);
        paint.setColor(Color.rgb(232, 241, 247));
        canvas.drawRect(40, 120, 555, 146, paint);
        paint.setColor(navy);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(8);
        canvas.drawText("#", 42, 137, paint);
        canvas.drawText("Booking", 62, 137, paint);
        canvas.drawText("Item", 140, 137, paint);
        canvas.drawText("Date", 245, 137, paint);
        canvas.drawText("Status", 365, 137, paint);
        canvas.drawText("Payment", 440, 137, paint);
        canvas.drawText("Amount", 505, 137, paint);
    }

    private String value(Object value) {
        return value == null ? "N/A" : String.valueOf(value);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

    private void applySearch(CharSequence query) {
        String term = query == null ? "" : query.toString().trim().toLowerCase(java.util.Locale.US);
        if (currentTab == 0) {
            List<Booking> filtered = new ArrayList<>();
            for (Booking booking : serviceBookings) {
                String text = ((booking.getStatus() == null ? "" : booking.getStatus()) + " "
                        + booking.getId()).toLowerCase(java.util.Locale.US);
                if (term.isEmpty() || text.contains(term)) filtered.add(booking);
            }
            bookingAdapter.setBookings(filtered);
        } else {
            List<GuideBooking> filtered = new ArrayList<>();
            for (GuideBooking booking : guideBookings) {
                String text = ((booking.getStatus() == null ? "" : booking.getStatus()) + " "
                        + booking.getScheduleDate() + " " + booking.getId()).toLowerCase(java.util.Locale.US);
                if (term.isEmpty() || text.contains(term)) filtered.add(booking);
            }
            guideBookingAdapter.setBookings(filtered);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCurrentTab();
    }

    private void setupAdapters() {
        bookingAdapter = new BookingAdapter(booking -> {
            if (booking != null && booking.getId() != null) {
                Intent intent = new Intent(requireContext(), BookingDetailActivity.class);
                intent.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, booking.getId());
                startActivity(intent);
            }
        });

        guideBookingAdapter = new GuideBookingAdapter(new GuideBookingAdapter.OnGuideBookingActionListener() {
            @Override
            public void onBookingClick(GuideBooking booking) {
                if (booking != null && booking.getId() != null) {
                    Intent intent = new Intent(requireContext(), GuideBookingDetailActivity.class);
                    intent.putExtra(GuideBookingDetailActivity.EXTRA_GUIDE_BOOKING_ID, booking.getId());
                    startActivity(intent);
                }
            }

            @Override
            public void onViewLocation(GuideBooking booking) {
                if (booking != null && booking.getTourGuideId() != null) {
                    Intent intent = new Intent(requireContext(), GuideLocationActivity.class);
                    intent.putExtra(GuideLocationActivity.EXTRA_GUIDE_ID, booking.getTourGuideId());
                    startActivity(intent);
                }
            }

            @Override
            public void onRateGuide(GuideBooking booking) {
                if (booking != null && booking.getId() != null) {
                    Intent intent = new Intent(requireContext(), RateGuideActivity.class);
                    intent.putExtra(RateGuideActivity.EXTRA_GUIDE_BOOKING_ID, booking.getId());
                    startActivity(intent);
                }
            }
        });

        binding.rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBookings.setAdapter(bookingAdapter);
    }

    private void setupListeners() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                if (currentTab == 0) {
                    binding.rvBookings.setAdapter(bookingAdapter);
                } else {
                    binding.rvBookings.setAdapter(guideBookingAdapter);
                }
                loadCurrentTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.swipeRefresh.setOnRefreshListener(this::loadCurrentTab);
        binding.btnRetry.setOnClickListener(v -> loadCurrentTab());
    }

    private void loadCurrentTab() {
        if (currentTab == 0) {
            loadServiceBookings();
        } else {
            loadGuideBookings();
        }
    }

    private void loadServiceBookings() {
        long userId = SessionManager.getInstance().getUserId();
        if (userId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        // Pre-fetch slots to enrich booking cards with human-readable titles
        ApiClient.getApiService().searchSlots(null, null, null, null, null, null, null, null).enqueue(new ApiCallback<List<ServiceSlot>>() {
            @Override
            public void onSuccess(List<ServiceSlot> slots) {
                if (!isSafe()) return;
                if (slots != null) {
                    Map<Long, ServiceSlot> map = new HashMap<>();
                    for (ServiceSlot s : slots) {
                        if (s != null && s.getId() != null) {
                            map.put(s.getId(), s);
                        }
                    }
                    bookingAdapter.setSlotCache(map);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                // Non-fatal: individual slots will still be queried on-demand by the adapter
            }
        });

        ApiClient.getApiService().getBookingsForTourist(userId).enqueue(new ApiCallback<List<Booking>>() {
            @Override
            public void onSuccess(List<Booking> bookings) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (bookings == null || bookings.isEmpty()) {
                    serviceBookings.clear();
                    bookingAdapter.setBookings(new ArrayList<>());
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.tvEmptyTitle.setText(R.string.bookings_empty_title);
                    binding.tvEmptyMessage.setText(R.string.bookings_empty_desc);
                } else {
                    serviceBookings.clear();
                    serviceBookings.addAll(bookings);
                    applySearch(binding.etSearchBookings.getText());
                    binding.layoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_generic));
            }
        });
    }

    private void loadGuideBookings() {
        long userId = SessionManager.getInstance().getUserId();
        if (userId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        // Pre-fetch guides to enrich guide bookings with human-readable guide names
        ApiClient.getApiService().getGuides(null).enqueue(new ApiCallback<List<Guide>>() {
            @Override
            public void onSuccess(List<Guide> guides) {
                if (!isSafe()) return;
                if (guides != null) {
                    Map<Long, Guide> map = new HashMap<>();
                    for (Guide g : guides) {
                        if (g != null && g.getId() != null) {
                            map.put(g.getId(), g);
                        }
                    }
                    guideBookingAdapter.setGuideCache(map);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                // Non-fatal: individual guides will still be queried on-demand by the adapter
            }
        });

        ApiClient.getApiService().getGuideBookingsForTourist(userId).enqueue(new ApiCallback<List<GuideBooking>>() {
            @Override
            public void onSuccess(List<GuideBooking> bookings) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (bookings == null || bookings.isEmpty()) {
                    guideBookings.clear();
                    guideBookingAdapter.setBookings(new ArrayList<>());
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.tvEmptyTitle.setText(R.string.guide_bookings_empty_title);
                    binding.tvEmptyMessage.setText(R.string.guide_bookings_empty_desc);
                } else {
                    guideBookings.clear();
                    guideBookings.addAll(bookings);
                    applySearch(binding.etSearchBookings.getText());
                    binding.layoutEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_generic));
            }
        });
    }

    private void showPayGuideDialog(GuideBooking booking) {
        if (booking == null || booking.getId() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_pay_guide_fee)
                .setMessage(String.format(java.util.Locale.US, "Confirm payment of $%.2f for this guide booking?", booking.getAgreedPrice()))
                .setPositiveButton("Pay via Card", (dialog, which) -> {
                    processGuidePayment(booking, Constants.PaymentMethod.CARD);
                })
                .setNeutralButton("Pay via Mobile Banking", (dialog, which) -> {
                    processGuidePayment(booking, Constants.PaymentMethod.MOBILE_BANKING);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void processGuidePayment(GuideBooking booking, String method) {
        PaymentRequest paymentRequest = new PaymentRequest(
                null,
                booking.getId(),
                booking.getAgreedPrice(),
                method
        );

        binding.progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService().createPayment(paymentRequest).enqueue(new ApiCallback<Payment>() {
            @Override
            public void onSuccess(Payment payment) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.toast_payment_processed_succes, Toast.LENGTH_SHORT).show();
                loadGuideBookings();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message != null ? message : "Payment failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCancelGuideBookingDialog(GuideBooking booking) {
        if (booking == null || booking.getId() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_cancel_guide_booking_title)
                .setMessage(R.string.confirm_cancel_guide_booking_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> cancelGuideBooking(booking))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void cancelGuideBooking(GuideBooking booking) {
        if (booking == null || booking.getId() == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().updateGuideBookingStatus(
                booking.getId(),
                new UpdateGuideBookingStatusRequest(Constants.GuideBookingStatus.DECLINED)
        ).enqueue(new ApiCallback<GuideBooking>() {
            @Override
            public void onSuccess(GuideBooking data) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.guide_booking_cancelled_success, Toast.LENGTH_SHORT).show();
                loadGuideBookings();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
