package com.example.horizontravel.ui.tourist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.databinding.ItemGuideBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> {

    public interface OnGuideClickListener {
        void onGuideClick(Guide guide);
    }

    private final List<Guide> guides = new ArrayList<>();
    private final OnGuideClickListener listener;

    public GuideAdapter(OnGuideClickListener listener) {
        this.listener = listener;
    }

    public void setGuides(List<Guide> newGuides) {
        this.guides.clear();
        if (newGuides != null) {
            this.guides.addAll(newGuides);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideBinding binding = ItemGuideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GuideViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        holder.bind(guides.get(position));
    }

    @Override
    public int getItemCount() {
        return guides.size();
    }

    class GuideViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuideBinding binding;

        GuideViewHolder(ItemGuideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Guide guide) {
            if (guide == null) return;

            binding.tvGuideName.setText(guide.getFullName() != null ? guide.getFullName() : "Tour Guide");

            boolean available = Boolean.TRUE.equals(guide.getAvailable());
            binding.tvAvailabilityBadge.setText(available ? "AVAILABLE" : "UNAVAILABLE");
            binding.tvAvailabilityBadge.setTextColor(binding.getRoot().getContext().getResources()
                    .getColor(R.color.badge_text));

            String location = guide.getLocation() != null && !guide.getLocation().isEmpty()
                    ? guide.getLocation() : "Location not specified";
            binding.tvGuideLocation.setText(location);

            if (guide.getLanguages() != null && !guide.getLanguages().isEmpty()) {
                binding.tvLanguages.setText("Languages: " + String.join(", ", guide.getLanguages()));
                binding.tvLanguages.setVisibility(View.VISIBLE);
            } else {
                binding.tvLanguages.setVisibility(View.GONE);
            }

            if (guide.getRatingAvg() != null && guide.getRatingCount() != null && guide.getRatingCount() > 0) {
                binding.layoutGuideRating.setVisibility(View.VISIBLE);
                binding.tvGuideRating.setText(String.format(Locale.US, "%.1f (%d)",
                        guide.getRatingAvg(), guide.getRatingCount()));
            } else {
                binding.layoutGuideRating.setVisibility(View.GONE);
            }

            int exp = guide.getExperienceYears() != null ? guide.getExperienceYears() : 0;
            binding.tvExperience.setText(
                    binding.getRoot().getContext().getString(R.string.guide_experience_years, exp));

            boolean negotiable = Boolean.TRUE.equals(guide.getNegotiable());
            binding.tvNegotiableBadge.setVisibility(negotiable ? View.VISIBLE : View.GONE);

            double price = guide.getDefaultPrice() != null ? guide.getDefaultPrice() : 0.0;
            binding.tvPricePerDay.setText(
                    binding.getRoot().getContext().getString(R.string.guide_rate_per_day, Formatters.formatCurrency(price)));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGuideClick(guide);
                }
            });
        }
    }
}
