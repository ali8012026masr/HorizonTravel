package com.example.horizontravel.ui.shared;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.data.model.Notification;
import com.example.horizontravel.databinding.ItemNotificationBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification, int position);
    }

    private final List<Notification> items = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Notification> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void markItemRead(int position) {
        if (position >= 0 && position < items.size()) {
            Notification item = items.get(position);
            item.setRead(true);
            notifyItemChanged(position);
        }
    }

    public void markAllRead() {
        for (Notification n : items) {
            n.setRead(true);
        }
        notifyDataSetChanged();
    }

    public List<Notification> getItems() {
        return items;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemNotificationBinding binding;

        NotificationViewHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Notification notification) {
            binding.tvMessage.setText(notification.getMessage());

            boolean isRead = Boolean.TRUE.equals(notification.getRead());
            binding.dotUnread.setVisibility(isRead ? View.GONE : View.VISIBLE);
            binding.tvMessage.setTypeface(null, isRead ? Typeface.NORMAL : Typeface.BOLD);

            if (notification.getType() != null && !notification.getType().isEmpty()) {
                binding.tvType.setVisibility(View.VISIBLE);
                binding.tvType.setText(Formatters.capitalizeEnum(notification.getType()));
            } else {
                binding.tvType.setVisibility(View.GONE);
            }

            if (notification.getCreatedAt() != null) {
                binding.tvDate.setText(Formatters.formatDateTime(notification.getCreatedAt()));
            } else {
                binding.tvDate.setText("");
            }

            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(items.get(pos), pos);
                }
            });
        }
    }
}
