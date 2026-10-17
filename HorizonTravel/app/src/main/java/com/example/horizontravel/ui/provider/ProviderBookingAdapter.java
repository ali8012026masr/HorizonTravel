package com.example.horizontravel.ui.provider;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Booking;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.databinding.ItemProviderBookingBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class ProviderBookingAdapter extends RecyclerView.Adapter<ProviderBookingAdapter.BookingViewHolder> {

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    private final List<Booking> items = new ArrayList<>();
    private final OnBookingClickListener listener;

    public ProviderBookingAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Booking> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProviderBookingBinding binding = ItemProviderBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ItemProviderBookingBinding binding;

        BookingViewHolder(ItemProviderBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Booking booking) {
            Context context = binding.getRoot().getContext();

            binding.tvBookingId.setText(context.getString(R.string.booking_id_item_format, booking.getId()));

            String status = booking.getStatus() != null ? booking.getStatus() : Constants.BookingStatus.PENDING;
            binding.tvBookingStatus.setText(status);

            int colorRes;
            if (Constants.BookingStatus.CONFIRMED.equalsIgnoreCase(status) || Constants.BookingStatus.COMPLETED.equalsIgnoreCase(status)) {
                colorRes = R.color.badge_success;
            } else if (Constants.BookingStatus.CANCELLED.equalsIgnoreCase(status)) {
                colorRes = R.color.badge_error;
            } else {
                colorRes = R.color.badge_pending;
            }
            binding.tvBookingStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, colorRes)));
            binding.tvBookingStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_text));

            long touristId = booking.getTouristId() != null ? booking.getTouristId() : 0;
            long slotId = booking.getServiceSlotId() != null ? booking.getServiceSlotId() : 0;
            binding.tvTouristAndSlot.setText(context.getString(R.string.booking_tourist_item_format, touristId, slotId));

            String formattedDate = Formatters.formatDateTime(booking.getBookingDate());
            binding.tvBookingDate.setText(String.format(java.util.Locale.US, "Booked on: %s", formattedDate.isEmpty() ? "N/A" : formattedDate));

            String paymentStatus = booking.getPaymentStatus() != null ? booking.getPaymentStatus() : Constants.PaymentStatus.UNPAID;
            String amountFormatted = Formatters.formatCurrency(booking.getTotalAmount());
            binding.tvTotalAmountAndPayment.setText(context.getString(R.string.booking_amount_format, amountFormatted, paymentStatus));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            });
        }
    }
}
