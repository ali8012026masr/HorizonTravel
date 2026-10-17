package com.example.horizontravel.ui.tourist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.ItemCartBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemActionListener {
        void onRemoveItem(ServiceSlot slot);
    }

    private final List<ServiceSlot> items = new ArrayList<>();
    private final OnCartItemActionListener listener;

    public CartAdapter(OnCartItemActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ServiceSlot> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ServiceSlot slot) {
            if (slot == null) return;

            binding.tvCartItemCategory.setText(slot.getCategory() != null ? slot.getCategory() : "SERVICE");

            String title = slot.getLocationName();
            if (title == null || title.trim().isEmpty()) {
                if (slot.getOrigin() != null && slot.getDestination() != null) {
                    title = slot.getOrigin() + " → " + slot.getDestination();
                } else {
                    title = slot.getCategory() != null ? slot.getCategory() : "Service Slot";
                }
            }
            binding.tvCartItemTitle.setText(title);

            if (slot.getProviderBusinessName() != null && !slot.getProviderBusinessName().isEmpty()) {
                binding.tvCartItemProvider.setText(slot.getProviderBusinessName());
                binding.tvCartItemProvider.setVisibility(View.VISIBLE);
            } else {
                binding.tvCartItemProvider.setVisibility(View.GONE);
            }

            String date = slot.getStartDateTime() != null ? Formatters.formatDateTime(slot.getStartDateTime()) : "";
            binding.tvCartItemDates.setText(date);
            binding.tvCartItemDates.setVisibility(date.isEmpty() ? View.GONE : View.VISIBLE);

            binding.tvCartItemPrice.setText(Formatters.formatCurrency(slot.getPrice()));

            binding.btnRemoveCartItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(slot);
                }
            });
        }
    }
}
