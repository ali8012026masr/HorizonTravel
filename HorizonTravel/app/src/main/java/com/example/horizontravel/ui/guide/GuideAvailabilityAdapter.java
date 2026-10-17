package com.example.horizontravel.ui.guide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.GuideAvailability;
import com.example.horizontravel.databinding.ItemGuideAvailabilityBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class GuideAvailabilityAdapter extends RecyclerView.Adapter<GuideAvailabilityAdapter.ViewHolder> {

    public interface OnAvailabilityClickListener {
        void onAvailabilityClick(GuideAvailability item);
    }

    private final List<GuideAvailability> items = new ArrayList<>();
    private final OnAvailabilityClickListener listener;

    public GuideAvailabilityAdapter(OnAvailabilityClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<GuideAvailability> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideAvailabilityBinding binding = ItemGuideAvailabilityBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuideAvailabilityBinding binding;

        ViewHolder(ItemGuideAvailabilityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GuideAvailability item) {
            if (item == null) return;

            String dateStr = item.getDate() != null ? Formatters.formatDate(item.getDate()) : "Unknown Date";
            binding.tvDate.setText(dateStr);

            String start = item.getStartTime() != null ? item.getStartTime() : "";
            String end = item.getEndTime() != null ? item.getEndTime() : "";
            binding.tvTimeRange.setText(String.format(java.util.Locale.US, "Time: %s - %s", start, end));

            if (item.getLocation() != null && !item.getLocation().trim().isEmpty()) {
                binding.tvLocation.setText(String.format(java.util.Locale.US, "Location: %s", item.getLocation()));
                binding.tvLocation.setVisibility(View.VISIBLE);
            } else {
                binding.tvLocation.setVisibility(View.GONE);
            }

            if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
                binding.tvNotes.setText(item.getNotes());
                binding.tvNotes.setVisibility(View.VISIBLE);
            } else {
                binding.tvNotes.setVisibility(View.GONE);
            }

            String status = item.getStatus() != null ? item.getStatus().toUpperCase() : "OPEN";
            binding.tvStatus.setText(status);

            int statusColor;
            if (Constants.AvailabilityStatus.OPEN.equalsIgnoreCase(status)) {
                statusColor = binding.getRoot().getContext().getResources().getColor(R.color.badge_success);
            } else if (Constants.AvailabilityStatus.BOOKED.equalsIgnoreCase(status)) {
                statusColor = binding.getRoot().getContext().getResources().getColor(R.color.badge_info);
            } else {
                statusColor = binding.getRoot().getContext().getResources().getColor(R.color.badge_error);
            }
            binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(statusColor));
            binding.tvStatus.setTextColor(binding.getRoot().getContext().getResources()
                    .getColor(R.color.badge_text));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onAvailabilityClick(item);
            });
        }
    }
}
