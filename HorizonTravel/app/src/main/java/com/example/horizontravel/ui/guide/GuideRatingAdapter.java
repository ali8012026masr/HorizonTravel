package com.example.horizontravel.ui.guide;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.data.model.GuideRating;
import com.example.horizontravel.databinding.ItemGuideReviewBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuideRatingAdapter extends RecyclerView.Adapter<GuideRatingAdapter.RatingViewHolder> {

    private final List<GuideRating> ratings = new ArrayList<>();

    public void setRatings(List<GuideRating> newRatings) {
        ratings.clear();
        if (newRatings != null) {
            ratings.addAll(newRatings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuideReviewBinding binding = ItemGuideReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RatingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        holder.bind(ratings.get(position));
    }

    @Override
    public int getItemCount() {
        return ratings.size();
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuideReviewBinding binding;

        RatingViewHolder(ItemGuideReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GuideRating rating) {
            if (rating == null) return;

            binding.tvRatingScore.setText(String.format(Locale.US, "★ %d / 5", rating.getScore()));

            String dateStr = rating.getRatedDate() != null
                    ? Formatters.formatDate(rating.getRatedDate()) : "";
            binding.tvRatingDate.setText(dateStr);

            String comment = rating.getComment() != null && !rating.getComment().trim().isEmpty()
                    ? rating.getComment() : "No comment provided.";
            binding.tvComment.setText(comment);
        }
    }
}
