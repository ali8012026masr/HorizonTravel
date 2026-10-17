package com.example.horizontravel.ui.tourist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Group;
import com.example.horizontravel.databinding.ItemGroupBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    private final List<Group> groups = new ArrayList<>();
    private final OnGroupClickListener listener;

    public GroupAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    public void setGroups(List<Group> newGroups) {
        this.groups.clear();
        if (newGroups != null) {
            this.groups.addAll(newGroups);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private final ItemGroupBinding binding;

        GroupViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Group group) {
            if (group == null) return;

            binding.tvGroupName.setText(group.getGroupName() != null ? group.getGroupName() : "Travel Group");

            String code = group.getJoinCode() != null ? group.getJoinCode() : "";
            binding.tvJoinCode.setText(binding.getRoot().getContext().getString(R.string.group_code_label, code));

            int count = group.getMembers() != null ? group.getMembers().size() : 1;
            binding.tvMemberCount.setText(String.format(java.util.Locale.US, "%d member(s)", count));

            if (group.getBookingId() != null && group.getBookingId() > 0) {
                binding.tvBookingLinked.setVisibility(View.VISIBLE);
                binding.tvBookingLinked.setText(String.format(java.util.Locale.US, "Linked to Booking #%d", group.getBookingId()));
            } else {
                binding.tvBookingLinked.setVisibility(View.GONE);
            }

            String date = group.getCreatedAt() != null ? Formatters.formatDate(group.getCreatedAt()) : "";
            binding.tvCreatedAt.setText(String.format(java.util.Locale.US, "Created: %s", date));

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGroupClick(group);
                }
            });
        }
    }
}
