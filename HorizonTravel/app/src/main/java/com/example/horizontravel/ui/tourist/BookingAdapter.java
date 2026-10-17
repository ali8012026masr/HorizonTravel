package com.example.horizontravel.ui.tourist;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Booking;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.ItemBookingBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final Map<Long, ServiceSlot> slotCache = new HashMap<>();
    private final Set<Long> pendingSlotFetches = new HashSet<>();
    private final OnBookingClickListener listener;

    public BookingAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> newBookings) {
        this.bookings.clear();
        if (newBookings != null) {
            this.bookings.addAll(newBookings);
        }
        notifyDataSetChanged();
    }

    public void setSlotCache(Map<Long, ServiceSlot> slots) {
        if (slots != null) {
            this.slotCache.putAll(slots);
            notifyDataSetChanged();
        }
    }

    public void putSlot(ServiceSlot slot) {
        if (slot != null && slot.getId() != null) {
            this.slotCache.put(slot.getId(), slot);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingBinding binding = ItemBookingBinding.inflate(
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
        private final ItemBookingBinding binding;

        BookingViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Booking booking) {
            if (booking == null) return;

            long id = booking.getId() != null ? booking.getId() : 0;
            binding.tvBookingId.setText(binding.getRoot().getContext().getString(R.string.booking_id_prefix, id));

            String status = booking.getStatus() != null ? booking.getStatus() : "UNKNOWN";
            binding.tvBookingStatus.setText(status);

            long slotId = booking.getServiceSlotId() != null ? booking.getServiceSlotId() : 0;
            ServiceSlot slot = slotCache.get(slotId);
            if (slot != null) {
                binding.tvSlotInfo.setText(formatServiceTitle(slot));
            } else if (slotId > 0) {
                binding.tvSlotInfo.setText(R.string.loading_service_details);
                fetchSlot(slotId);
            } else {
                binding.tvSlotInfo.setText(R.string.travel_service);
            }

            String date = booking.getBookingDate() != null ? Formatters.formatDateTime(booking.getBookingDate()) : "";
            binding.tvBookingDate.setText(binding.getRoot().getContext().getString(R.string.booked_on_format, date));

            String payment = booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "UNPAID";
            binding.tvPaymentStatus.setText(binding.getRoot().getContext().getString(R.string.booking_payment_label, payment));

            binding.tvBookingAmount.setText(Formatters.formatCurrency(booking.getTotalAmount()));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            });
        }

        private void fetchSlot(long slotId) {
            if (pendingSlotFetches.contains(slotId)) return;
            pendingSlotFetches.add(slotId);

            ApiClient.getApiService().getSlotById(slotId).enqueue(new ApiCallback<ServiceSlot>() {
                @Override
                public void onSuccess(ServiceSlot data) {
                    if (data != null && data.getId() != null) {
                        slotCache.put(data.getId(), data);
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onError(String message, int statusCode) {
                    ServiceSlot fallback = new ServiceSlot();
                    fallback.setId(slotId);
                    slotCache.put(slotId, fallback);
                }
            });
        }

        private String formatServiceTitle(ServiceSlot slot) {
            if (slot == null) return binding.getRoot().getContext().getString(R.string.travel_service);

            String title = slot.getLocationName();
            if (title != null && !title.trim().isEmpty()) {
                if (slot.getProviderBusinessName() != null && !slot.getProviderBusinessName().trim().isEmpty()) {
                    return title + " (" + slot.getProviderBusinessName() + ")";
                }
                return title;
            }

            if (slot.getOrigin() != null && slot.getDestination() != null) {
                String route = slot.getOrigin() + " → " + slot.getDestination();
                if (slot.getProviderBusinessName() != null && !slot.getProviderBusinessName().trim().isEmpty()) {
                    return route + " • " + slot.getProviderBusinessName();
                }
                return route;
            }

            if (slot.getDestination() != null) {
                if (slot.getProviderBusinessName() != null && !slot.getProviderBusinessName().trim().isEmpty()) {
                    return slot.getDestination() + " • " + slot.getProviderBusinessName();
                }
                return slot.getDestination();
            }

            if (slot.getCategory() != null) {
                String cat = Formatters.capitalizeEnum(slot.getCategory());
                if (slot.getProviderBusinessName() != null && !slot.getProviderBusinessName().trim().isEmpty()) {
                    return cat + " • " + slot.getProviderBusinessName();
                }
                return cat + " Service";
            }

            return binding.getRoot().getContext().getString(R.string.travel_service);
        }
    }
}
