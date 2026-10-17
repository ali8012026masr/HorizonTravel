package com.example.horizontravel.ui.tourist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.data.model.GroupMessage;
import com.example.horizontravel.databinding.ItemChatMessageBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.MessageViewHolder> {

    private final List<GroupMessage> messages = new ArrayList<>();
    private final long currentUserId;

    public GroupChatAdapter(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<GroupMessage> newMessages) {
        this.messages.clear();
        if (newMessages != null) {
            this.messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageBinding binding;

        MessageViewHolder(ItemChatMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupMessage msg) {
            if (msg == null) return;

            boolean isMe = msg.getSenderId() != null && msg.getSenderId() == currentUserId;
            String timeText = msg.getSentAt() != null ? Formatters.formatDateTime(msg.getSentAt()) : "";

            if (isMe) {
                binding.layoutOutgoing.setVisibility(View.VISIBLE);
                binding.layoutIncoming.setVisibility(View.GONE);

                binding.tvOutgoingMessage.setText(msg.getMessage());
                binding.tvOutgoingTime.setText(timeText);
            } else {
                binding.layoutOutgoing.setVisibility(View.GONE);
                binding.layoutIncoming.setVisibility(View.VISIBLE);

                String sender = msg.getSenderName() != null ? msg.getSenderName() : "Member #" + msg.getSenderId();
                binding.tvSenderName.setText(sender);
                binding.tvIncomingMessage.setText(msg.getMessage());
                binding.tvIncomingTime.setText(timeText);
            }
        }
    }
}
