package com.example.horizontravel.ui.tourist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.ItemSlotBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    public interface OnSlotClickListener {
        void onSlotClick(ServiceSlot slot);
    }

    private final List<ServiceSlot> slots = new ArrayList<>();
    private final OnSlotClickListener listener;

    public SlotAdapter(OnSlotClickListener listener) {
        this.listener = listener;
    }

    public void setSlots(List<ServiceSlot> newSlots) {
        this.slots.clear();
        if (newSlots != null) {
            this.slots.addAll(newSlots);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSlotBinding binding = ItemSlotBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SlotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        holder.bind(slots.get(position));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    class SlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemSlotBinding binding;

        SlotViewHolder(ItemSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ServiceSlot slot) {
            if (slot == null) return;

            // Category
            binding.tvSlotCategory.setText(slot.getCategory() != null ? slot.getCategory() : "SERVICE");

            // Title
            String title = slot.getLocationName();
            if (title == null || title.trim().isEmpty()) {
                if (slot.getOrigin() != null && slot.getDestination() != null) {
                    title = slot.getOrigin() + " → " + slot.getDestination();
                } else if (slot.getDestination() != null) {
                    title = slot.getDestination();
                } else {
                    title = slot.getCategory() != null ? slot.getCategory() : "Travel Slot";
                }
            }
            binding.tvSlotTitle.setText(title);

            // Provider
            if (slot.getProviderBusinessName() != null && !slot.getProviderBusinessName().isEmpty()) {
                binding.tvProviderName.setText(slot.getProviderBusinessName());
                binding.tvProviderName.setVisibility(View.VISIBLE);
            } else {
                binding.tvProviderName.setVisibility(View.GONE);
            }

            // Dates
            String dateText = "";
            if (slot.getStartDateTime() != null) {
                dateText = Formatters.formatDateTime(slot.getStartDateTime());
                if (slot.getEndDateTime() != null) {
                    dateText += " - " + Formatters.formatDateTime(slot.getEndDateTime());
                }
            }
            binding.tvDateTime.setText(dateText);

            // Seats
            int seats = slot.getAvailableSeats();
            binding.tvAvailableSeats.setText(
                    binding.getRoot().getContext().getString(R.string.slot_available_seats, seats));

            // Price
            binding.tvPrice.setText(Formatters.formatCurrency(slot.getPrice()));

            // Rating
            if (slot.getProviderRatingAvg() != null && slot.getProviderRatingCount() != null && slot.getProviderRatingCount() > 0) {
                binding.layoutRating.setVisibility(View.VISIBLE);
                binding.tvRating.setText(String.format(java.util.Locale.US, "%.1f (%d)",
                        slot.getProviderRatingAvg(), slot.getProviderRatingCount()));
            } else {
                binding.layoutRating.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSlotClick(slot);
                }
            });
        }
    }
}
