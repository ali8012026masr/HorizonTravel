package com.example.horizontravel.ui.guide;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.GuideBooking;
import com.example.horizontravel.databinding.ItemGuideIncomingBookingBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class GuideIncomingBookingAdapter extends RecyclerView.Adapter<GuideIncomingBookingAdapter.BookingViewHolder> {

    public interface OnBookingActionListener {
        void onAccept(GuideBooking booking);
        void onDecline(GuideBooking booking);
        void onMarkCompleted(GuideBooking booking);
        void onMarkPaymentReceived(GuideBooking booking);
        void onItemClick(GuideBooking booking);
    }

    private final List<GuideBooking> bookings = new ArrayList<>();
    private final OnBookingActionListener listener;
    private final boolean showActions;

    public GuideIncomingBookingAdapter(OnBookingActionListener listener, boolean showActions) {
        this.listener = listener;
        this.showActions = showActions;
    }

    public void setBookings(List<GuideBooking> newBookings) {
        bookings.clear();
        if (newBookings != null) {
            bookings.addAll(newBookings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideIncomingBookingBinding binding = ItemGuideIncomingBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuideIncomingBookingBinding binding;

        BookingViewHolder(ItemGuideIncomingBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GuideBooking booking) {
            if (booking == null) return;

            long id = booking.getId() != null ? booking.getId() : 0;
            binding.tvBookingId.setText(binding.getRoot().getContext().getString(R.string.guide_request_id_prefix, id));

            long touristId = booking.getTouristId() != null ? booking.getTouristId() : 0;
            binding.tvTouristInfo.setText(binding.getRoot().getContext().getString(R.string.tourist_id_prefix, touristId));

            String scheduleDate = booking.getScheduleDate() != null
                    ? Formatters.formatDate(booking.getScheduleDate()) : "Date not specified";
            binding.tvScheduleDate.setText(String.format(java.util.Locale.US, "Scheduled Date: %s", scheduleDate));

            binding.tvPrice.setText(Formatters.formatCurrency(booking.getAgreedPrice()));

            boolean negotiated = Boolean.TRUE.equals(booking.getNegotiated());
            binding.tvNegotiatedBadge.setVisibility(negotiated ? View.VISIBLE : View.GONE);

            String status = booking.getStatus() != null ? booking.getStatus().toUpperCase() : "REQUESTED";
            binding.tvStatusBadge.setText(status);

            int statusColor;
            if (Constants.GuideBookingStatus.ACCEPTED.equalsIgnoreCase(status)) {
                statusColor = binding.getRoot().getContext().getResources().getColor(R.color.badge_success);
            } else if (Constants.GuideBookingStatus.COMPLETED.equalsIgnoreCase(status)) {
                statusColor = binding.getRoot().getContext().getResources().getColor(R.color.badge_info);
            } else if (Constants.GuideBookingStatus.DECLINED.equalsIgnoreCase(status)) {
                statusColor = binding.getRoot().getContext().getResources().getColor(R.color.badge_error);
            } else {
                statusColor = binding.getRoot().getContext().getResources().getColor(R.color.badge_pending);
            }
            binding.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(statusColor));
            binding.tvStatusBadge.setTextColor(binding.getRoot().getContext().getResources().getColor(R.color.badge_text));

            boolean paymentReceived = Boolean.TRUE.equals(booking.getPaymentReceived());
            binding.tvPaymentStatus.setText(paymentReceived ? "PAID" : "UNPAID");
            binding.tvPaymentStatus.setTextColor(binding.getRoot().getContext().getResources().getColor(
                    paymentReceived ? R.color.teal_secondary : R.color.tertiary));

            if (!showActions) {
                binding.layoutActions.setVisibility(View.GONE);
            } else {
                if (Constants.GuideBookingStatus.REQUESTED.equalsIgnoreCase(status)) {
                    binding.layoutActions.setVisibility(View.VISIBLE);
                    binding.btnAccept.setVisibility(View.VISIBLE);
                    binding.btnDecline.setVisibility(View.VISIBLE);
                    binding.btnMarkPayment.setVisibility(View.GONE);
                    binding.btnMarkCompleted.setVisibility(View.GONE);
                } else if (Constants.GuideBookingStatus.ACCEPTED.equalsIgnoreCase(status)) {
                    binding.layoutActions.setVisibility(View.VISIBLE);
                    binding.btnAccept.setVisibility(View.GONE);
                    binding.btnDecline.setVisibility(View.GONE);
                    binding.btnMarkCompleted.setVisibility(View.VISIBLE);

                    // Show Mark payment received if payment not marked yet and method is CASH
                    boolean isCash = Constants.PaymentMethod.CASH.equalsIgnoreCase(booking.getPaymentMethod());
                    if (!paymentReceived && isCash) {
                        binding.btnMarkPayment.setVisibility(View.VISIBLE);
                    } else {
                        binding.btnMarkPayment.setVisibility(View.GONE);
                    }
                } else {
                    binding.layoutActions.setVisibility(View.GONE);
                }
            }

            binding.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(booking);
            });

            binding.btnDecline.setOnClickListener(v -> {
                if (listener != null) listener.onDecline(booking);
            });

            binding.btnMarkCompleted.setOnClickListener(v -> {
                if (listener != null) listener.onMarkCompleted(booking);
            });

            binding.btnMarkPayment.setOnClickListener(v -> {
                if (listener != null) listener.onMarkPaymentReceived(booking);
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(booking);
            });
        }
    }
}
