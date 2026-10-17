package com.example.horizontravel.ui.provider;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.ItemProviderSlotBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class ProviderSlotAdapter extends RecyclerView.Adapter<ProviderSlotAdapter.SlotViewHolder> {

    public interface OnSlotClickListener {
        void onSlotClick(ServiceSlot slot);
    }

    private final List<ServiceSlot> items = new ArrayList<>();
    private final OnSlotClickListener listener;

    public ProviderSlotAdapter(OnSlotClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ServiceSlot> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProviderSlotBinding binding = ItemProviderSlotBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SlotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemProviderSlotBinding binding;

        SlotViewHolder(ItemProviderSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ServiceSlot slot) {
            Context context = binding.getRoot().getContext();

            // Category & Status
            String category = slot.getCategory() != null ? Formatters.capitalizeEnum(slot.getCategory()) : "";
            binding.tvSlotCategory.setText(category);

            String status = slot.getStatus() != null ? slot.getStatus() : Constants.SlotStatus.OPEN;
            binding.tvSlotStatus.setText(status);

            // Color-code the status chip
            int chipColorRes;
            if (Constants.SlotStatus.OPEN.equalsIgnoreCase(status)) {
                chipColorRes = R.color.badge_success;
            } else if (Constants.SlotStatus.FULL.equalsIgnoreCase(status)) {
                chipColorRes = R.color.badge_pending;
            } else if (Constants.SlotStatus.CANCELLED.equalsIgnoreCase(status)) {
                chipColorRes = R.color.badge_error;
            } else {
                chipColorRes = R.color.badge_neutral;
            }
            binding.tvSlotStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, chipColorRes)));
            binding.tvSlotStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_text));

            // Location or Route
            String origin = slot.getOrigin();
            String destination = slot.getDestination();
            String locationName = slot.getLocationName();

            if (origin != null && !origin.trim().isEmpty() && destination != null && !destination.trim().isEmpty()) {
                binding.tvSlotLocationOrRoute.setText(context.getString(R.string.slot_route_format, origin.trim(), destination.trim()));
            } else if (locationName != null && !locationName.trim().isEmpty()) {
                binding.tvSlotLocationOrRoute.setText(locationName.trim());
            } else if (origin != null && !origin.trim().isEmpty()) {
                binding.tvSlotLocationOrRoute.setText(origin.trim());
            } else if (destination != null && !destination.trim().isEmpty()) {
                binding.tvSlotLocationOrRoute.setText(destination.trim());
            } else {
                binding.tvSlotLocationOrRoute.setText(context.getString(R.string.slot_id_prefix, slot.getId()));
            }

            // Start & End Date Time
            String startFormatted = Formatters.formatDateTime(slot.getStartDateTime());
            String endFormatted = Formatters.formatDateTime(slot.getEndDateTime());
            if (!startFormatted.isEmpty() && !endFormatted.isEmpty()) {
                binding.tvSlotDateTime.setText(context.getString(R.string.slot_date_range_format, startFormatted, endFormatted));
            } else if (!startFormatted.isEmpty()) {
                binding.tvSlotDateTime.setText(startFormatted);
            } else {
                binding.tvSlotDateTime.setText(endFormatted);
            }

            // Seats & Price
            int capacity = slot.getCapacity() != null ? slot.getCapacity() : 0;
            int available = slot.getAvailableSeats() != null ? slot.getAvailableSeats() : capacity;
            binding.tvSlotSeats.setText(context.getString(R.string.slot_seats_format, available, capacity));

            binding.tvSlotPrice.setText(Formatters.formatCurrency(slot.getPrice()));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSlotClick(slot);
                }
            });
        }
    }
}
