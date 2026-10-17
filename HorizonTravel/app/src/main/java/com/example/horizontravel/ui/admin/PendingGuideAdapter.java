package com.example.horizontravel.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.databinding.ItemPendingGuideBinding;

import java.util.ArrayList;
import java.util.List;

public class PendingGuideAdapter extends RecyclerView.Adapter<PendingGuideAdapter.GuideViewHolder> {

    public interface OnPendingGuideActionListener {
        void onApprove(Guide guide);
        void onReject(Guide guide);
        void onDetails(Guide guide);
    }

    private final List<Guide> items = new ArrayList<>();
    private final OnPendingGuideActionListener listener;

    public PendingGuideAdapter(OnPendingGuideActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Guide> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPendingGuideBinding binding = ItemPendingGuideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GuideViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class GuideViewHolder extends RecyclerView.ViewHolder {
        private final ItemPendingGuideBinding binding;

        GuideViewHolder(ItemPendingGuideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Guide guide) {
            binding.tvGuideName.setText(guide.getFullName());

            StringBuilder details = new StringBuilder();
            if (guide.getLocation() != null && !guide.getLocation().trim().isEmpty()) {
                details.append("Location: ").append(guide.getLocation().trim()).append(" | ");
            }
            if (guide.getExperienceYears() != null) {
                details.append("Exp: ").append(guide.getExperienceYears()).append(" yrs | ");
            }
            if (guide.getLanguages() != null && !guide.getLanguages().isEmpty()) {
                details.append("Languages: ").append(String.join(", ", guide.getLanguages()));
            }
            String detailStr = details.toString().trim();
            if (detailStr.endsWith("|")) {
                detailStr = detailStr.substring(0, detailStr.length() - 1).trim();
            }
            binding.tvGuideDetails.setText(detailStr.isEmpty() ? "No additional details provided" : detailStr);

            binding.btnApproveGuide.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(guide);
                }
            });

            binding.btnRejectGuide.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReject(guide);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetails(guide);
                }
            });
        }
    }
}
