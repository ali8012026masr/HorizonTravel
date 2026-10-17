package com.example.horizontravel.ui.tourist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.data.model.GuideBooking;
import com.example.horizontravel.databinding.ItemGuideBookingBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GuideBookingAdapter extends RecyclerView.Adapter<GuideBookingAdapter.GuideBookingViewHolder> {

    public interface OnGuideBookingActionListener {
        void onBookingClick(GuideBooking booking);
        void onViewLocation(GuideBooking booking);
        void onRateGuide(GuideBooking booking);
    }

    private final List<GuideBooking> bookings = new ArrayList<>();
    private final Map<Long, Guide> guideCache = new HashMap<>();
    private final Set<Long> pendingGuideFetches = new HashSet<>();
    private final OnGuideBookingActionListener listener;

    public GuideBookingAdapter(OnGuideBookingActionListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<GuideBooking> newBookings) {
        this.bookings.clear();
        if (newBookings != null) {
            this.bookings.addAll(newBookings);
        }
        notifyDataSetChanged();
    }

    public void setGuideCache(Map<Long, Guide> guides) {
        if (guides != null) {
            this.guideCache.putAll(guides);
            notifyDataSetChanged();
        }
    }

    public void putGuide(Guide guide) {
        if (guide != null && guide.getId() != null) {
            this.guideCache.put(guide.getId(), guide);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public GuideBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideBookingBinding binding = ItemGuideBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GuideBookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideBookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class GuideBookingViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuideBookingBinding binding;

        GuideBookingViewHolder(ItemGuideBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GuideBooking booking) {
            if (booking == null) return;

            long id = booking.getId() != null ? booking.getId() : 0;
            binding.tvGuideBookingId.setText(binding.getRoot().getContext().getString(R.string.guide_hire_id_format, id));

            String status = booking.getStatus() != null ? booking.getStatus() : Constants.GuideBookingStatus.REQUESTED;
            binding.tvGuideBookingStatus.setText(status);

            long guideId = booking.getTourGuideId() != null ? booking.getTourGuideId() : 0;
            Guide guide = guideCache.get(guideId);
            if (guide != null && guide.getFullName() != null && !guide.getFullName().trim().isEmpty()) {
                if (guide.getLocation() != null && !guide.getLocation().trim().isEmpty()) {
                    binding.tvGuideInfo.setText(binding.getRoot().getContext().getString(
                            R.string.guide_name_with_location_format, guide.getFullName(), guide.getLocation()));
                } else {
                    binding.tvGuideInfo.setText(binding.getRoot().getContext().getString(
                            R.string.guide_name_format, guide.getFullName()));
                }
            } else if (guideId > 0) {
                binding.tvGuideInfo.setText(R.string.loading_guide_details);
                fetchGuide(guideId);
            } else {
                binding.tvGuideInfo.setText(R.string.licensed_tour_guide);
            }

            String date = booking.getScheduleDate() != null ? Formatters.formatDate(booking.getScheduleDate()) : "";
            binding.tvScheduleDate.setText(binding.getRoot().getContext().getString(R.string.tour_date_format, date));

            boolean paid = Boolean.TRUE.equals(booking.getPaymentReceived());
            binding.tvPaymentReceived.setText(binding.getRoot().getContext().getString(
                    paid ? R.string.payment_received_label : R.string.payment_pending_label));

            binding.tvAgreedPrice.setText(Formatters.formatCurrency(booking.getAgreedPrice()));

            // Action buttons
            boolean isAccepted = Constants.GuideBookingStatus.ACCEPTED.equalsIgnoreCase(status);
            boolean isCompleted = Constants.GuideBookingStatus.COMPLETED.equalsIgnoreCase(status);

            boolean hasActions = isAccepted || isCompleted;
            binding.layoutActions.setVisibility(hasActions ? View.VISIBLE : View.GONE);

            binding.btnLiveLocation.setVisibility(isAccepted ? View.VISIBLE : View.GONE);
            binding.btnRateGuide.setVisibility(isCompleted ? View.VISIBLE : View.GONE);

            binding.btnLiveLocation.setOnClickListener(v -> {
                if (listener != null) listener.onViewLocation(booking);
            });

            binding.btnRateGuide.setOnClickListener(v -> {
                if (listener != null) listener.onRateGuide(booking);
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onBookingClick(booking);
            });
        }

        private void fetchGuide(long guideId) {
            if (pendingGuideFetches.contains(guideId)) return;
            pendingGuideFetches.add(guideId);

            ApiClient.getApiService().getGuideById(guideId).enqueue(new ApiCallback<Guide>() {
                @Override
                public void onSuccess(Guide data) {
                    if (data != null && data.getId() != null) {
                        guideCache.put(data.getId(), data);
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onError(String message, int statusCode) {
                    Guide fallback = new Guide();
                    fallback.setId(guideId);
                    fallback.setFullName("Guide #" + guideId);
                    guideCache.put(guideId, fallback);
                }
            });
        }
    }
}
