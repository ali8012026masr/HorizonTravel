package com.example.horizontravel.ui.tourist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.databinding.ItemAiMessageBinding;

import java.util.ArrayList;
import java.util.List;

public class AiChatAdapter extends RecyclerView.Adapter<AiChatAdapter.AiMessageViewHolder> {

    public interface OnAiActionClickListener {
        void onFilterSlots(String payloadJson);
        void onRecommendBundle(String payloadJson);
    }

    public static class AiChatItem {
        public final boolean isUser;
        public final String text;
        public final String actionType;
        public final String actionPayloadJson;
        public final boolean isError;

        public AiChatItem(boolean isUser, String text, String actionType, String actionPayloadJson, boolean isError) {
            this.isUser = isUser;
            this.text = text;
            this.actionType = actionType;
            this.actionPayloadJson = actionPayloadJson;
            this.isError = isError;
        }
    }

    private final List<AiChatItem> items = new ArrayList<>();
    private final OnAiActionClickListener listener;

    public AiChatAdapter(OnAiActionClickListener listener) {
        this.listener = listener;
    }

    public void addItem(AiChatItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @NonNull
    @Override
    public AiMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAiMessageBinding binding = ItemAiMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AiMessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AiMessageViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class AiMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemAiMessageBinding binding;

        AiMessageViewHolder(ItemAiMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AiChatItem item) {
            if (item == null) return;

            if (item.isUser) {
                binding.layoutUserMessage.setVisibility(View.VISIBLE);
                binding.layoutAiMessage.setVisibility(View.GONE);
                binding.tvUserText.setText(item.text);
            } else {
                binding.layoutUserMessage.setVisibility(View.GONE);
                binding.layoutAiMessage.setVisibility(View.VISIBLE);
                binding.tvAiText.setText(item.text);

                boolean isFilter = "FILTER_SLOTS".equalsIgnoreCase(item.actionType);
                boolean isBundle = "RECOMMEND_BUNDLE".equalsIgnoreCase(item.actionType)
                        || "RECOMMEND_GROUP_BUNDLE".equalsIgnoreCase(item.actionType);

                if (isFilter || isBundle) {
                    binding.layoutAiActions.setVisibility(View.VISIBLE);
                    binding.btnActionFilter.setVisibility(isFilter ? View.VISIBLE : View.GONE);

                    if (isBundle) {
                        binding.cardMiniBundle.setVisibility(View.VISIBLE);
                        String title = "Recommended Bundle";
                        String desc = "Tap to view details";

                        if (item.actionPayloadJson != null && !item.actionPayloadJson.isEmpty()) {
                            try {
                                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(item.actionPayloadJson).getAsJsonObject();
                                if (json.has("name")) {
                                    title = json.get("name").getAsString();
                                }
                                if (json.has("price")) {
                                    desc = "Estimated Price: $" + json.get("price").getAsString();
                                }
                            } catch (Exception ignored) {}
                        }
                        binding.tvMiniCardTitle.setText(title);
                        binding.tvMiniCardDesc.setText(desc);
                    } else {
                        binding.cardMiniBundle.setVisibility(View.GONE);
                    }

                    binding.btnActionFilter.setOnClickListener(v -> {
                        if (listener != null) listener.onFilterSlots(item.actionPayloadJson);
                    });

                    binding.btnActionBundle.setOnClickListener(v -> {
                        if (listener != null) listener.onRecommendBundle(item.actionPayloadJson);
                    });
                } else {
                    binding.layoutAiActions.setVisibility(View.GONE);
                }
            }
        }
    }
}
