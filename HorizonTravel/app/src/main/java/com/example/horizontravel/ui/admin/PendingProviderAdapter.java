package com.example.horizontravel.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.data.model.Provider;
import com.example.horizontravel.databinding.ItemPendingProviderBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class PendingProviderAdapter extends RecyclerView.Adapter<PendingProviderAdapter.ProviderViewHolder> {

    public interface OnPendingProviderActionListener {
        void onApprove(Provider provider);
        void onReject(Provider provider);
        void onDetails(Provider provider);
    }

    private final List<Provider> items = new ArrayList<>();
    private final OnPendingProviderActionListener listener;

    public PendingProviderAdapter(OnPendingProviderActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Provider> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPendingProviderBinding binding = ItemPendingProviderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProviderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ProviderViewHolder extends RecyclerView.ViewHolder {
        private final ItemPendingProviderBinding binding;

        ProviderViewHolder(ItemPendingProviderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Provider provider) {
            String business = provider.getBusinessName();
            binding.tvProviderBusinessName.setText(business != null && !business.trim().isEmpty() ? business : provider.getFullName());

            String owner = provider.getFullName();
            binding.tvProviderOwner.setText(String.format(java.util.Locale.US, "Applicant: %s", owner != null ? owner : "N/A"));

            String category = provider.getCategory() != null ? Formatters.capitalizeEnum(provider.getCategory()) : "Unknown Category";
            String license = provider.getTradeLicenseNo() != null ? provider.getTradeLicenseNo() : "N/A";
            binding.tvProviderCategoryAndLicense.setText(String.format(java.util.Locale.US, "Category: %s | License: %s", category, license));

            binding.btnApproveProvider.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(provider);
                }
            });

            binding.btnRejectProvider.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReject(provider);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDetails(provider);
                }
            });
        }
    }
}
