package com.example.horizontravel.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.AdminUser;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.databinding.ItemAdminUserBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(AdminUser user);
    }

    private final List<AdminUser> items = new ArrayList<>();
    private final OnUserClickListener listener;

    public AdminUserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminUser> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminUserBinding binding = ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminUserBinding binding;

        UserViewHolder(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminUser user) {
            Context context = binding.getRoot().getContext();

            binding.tvUserName.setText(user.getFullName() != null ? user.getFullName() : "User #" + user.getId());
            binding.tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");

            String role = user.getRole() != null ? Formatters.capitalizeEnum(user.getRole()) : "Unknown Role";
            binding.tvUserRole.setText(String.format(java.util.Locale.US, "Role: %s", role));

            String status = user.getStatus() != null ? user.getStatus() : Constants.UserStatus.ACTIVE;
            binding.tvUserStatus.setText(status);

            int colorRes;
            if (Constants.UserStatus.ACTIVE.equalsIgnoreCase(status)) {
                colorRes = R.color.status_active;
            } else if (Constants.UserStatus.PENDING_VERIFICATION.equalsIgnoreCase(status)) {
                colorRes = R.color.status_pending;
            } else if (Constants.UserStatus.SUSPENDED.equalsIgnoreCase(status)) {
                colorRes = R.color.status_suspended;
            } else if (Constants.UserStatus.REVOKED.equalsIgnoreCase(status)) {
                colorRes = R.color.status_error;
            } else {
                colorRes = R.color.text_secondary;
            }
            binding.tvUserStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, colorRes)));
            binding.tvUserStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_text));

            String created = Formatters.formatDate(user.getCreatedAt());
            binding.tvUserCreated.setText(created.isEmpty() ? "" : String.format(java.util.Locale.US, "Joined: %s", created));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }
    }
}
