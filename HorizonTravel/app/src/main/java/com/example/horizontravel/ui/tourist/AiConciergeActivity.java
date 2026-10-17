package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.AiChatMessage;
import com.example.horizontravel.data.model.AiChatRequest;
import com.example.horizontravel.data.model.AiChatResponse;
import com.example.horizontravel.databinding.ActivityAiConciergeBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class AiConciergeActivity extends BaseActivity implements AiChatAdapter.OnAiActionClickListener {

    private ActivityAiConciergeBinding binding;
    private AiChatAdapter adapter;
    private final List<AiChatMessage> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiConciergeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        setupInput();
        addInitialWelcome();
    }

    private void setupRecyclerView() {
        adapter = new AiChatAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvAiMessages.setLayoutManager(layoutManager);
        binding.rvAiMessages.setAdapter(adapter);
    }

    private void setupInput() {
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int len = s != null ? s.length() : 0;
                binding.tvCharCount.setText(String.format(java.util.Locale.US, "%d / 1000", len));
                if (len > 1000) {
                    binding.tvCharCount.setTextColor(getResources().getColor(R.color.coral_accent));
                } else {
                    binding.tvCharCount.setTextColor(getResources().getColor(R.color.on_surface_variant));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void addInitialWelcome() {
        adapter.addItem(new AiChatAdapter.AiChatItem(
                false,
                getString(R.string.ai_concierge_welcome),
                null,
                null,
                false
        ));
    }

    private void sendMessage() {
        String text = binding.etMessage.getText() != null ? binding.etMessage.getText().toString().trim() : "";
        if (text.isEmpty() || text.length() > 1000) return;

        // Add to UI
        adapter.addItem(new AiChatAdapter.AiChatItem(true, text, null, null, false));
        binding.rvAiMessages.scrollToPosition(adapter.getItemCount() - 1);
        binding.etMessage.setText("");

        // Prepare request with capped history (max 12 turns)
        List<AiChatMessage> historyToSend = new ArrayList<>();
        int start = Math.max(0, history.size() - 12);
        for (int i = start; i < history.size(); i++) {
            historyToSend.add(history.get(i));
        }

        // Add user turn to local history
        history.add(new AiChatMessage("user", text));

        binding.progressBarThinking.setVisibility(View.VISIBLE);
        binding.btnSend.setEnabled(false);

        AiChatRequest request = new AiChatRequest(text, historyToSend);

        ApiClient.getApiService().sendAiChatMessage(request).enqueue(new ApiCallback<AiChatResponse>() {
            @Override
            public void onSuccess(AiChatResponse response) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBarThinking.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);

                if (response != null && response.getReplyText() != null) {
                    adapter.addItem(new AiChatAdapter.AiChatItem(
                            false,
                            response.getReplyText(),
                            response.getActionType(),
                            response.getActionPayloadJson(),
                            false
                    ));
                    history.add(new AiChatMessage("assistant", response.getReplyText()));
                } else {
                    adapter.addItem(new AiChatAdapter.AiChatItem(
                            false,
                            "I received an empty response. Please try asking again.",
                            null,
                            null,
                            false
                    ));
                }
                binding.rvAiMessages.scrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBarThinking.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);

                String errorText;
                if (statusCode == 429) {
                    errorText = getString(R.string.ai_rate_limit_error);
                } else {
                    errorText = getString(R.string.ai_error_bubble,
                            message != null ? message : "Unable to reach AI service.");
                }
                adapter.addItem(new AiChatAdapter.AiChatItem(false, errorText, null, null, true));
                binding.rvAiMessages.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }

    @Override
    public void onFilterSlots(String payloadJson) {
        String category = null;
        Double minPrice = null;
        Double maxPrice = null;
        String query = null;

        if (payloadJson != null && !payloadJson.isEmpty()) {
            try {
                JsonObject json = JsonParser.parseString(payloadJson).getAsJsonObject();
                if (json.has("category")) category = json.get("category").getAsString();
                if (json.has("minPrice")) minPrice = json.get("minPrice").getAsDouble();
                if (json.has("maxPrice")) maxPrice = json.get("maxPrice").getAsDouble();
                if (json.has("query")) query = json.get("query").getAsString();
                else if (json.has("destination")) query = json.get("destination").getAsString();
            } catch (Exception ignored) {}
        }

        Intent intent = new Intent(this, TouristMainActivity.class);
        intent.putExtra(TouristMainActivity.EXTRA_TARGET_TAB, R.id.nav_explore);
        if (category != null) intent.putExtra(TouristMainActivity.EXTRA_FILTER_CATEGORY, category);
        if (minPrice != null) intent.putExtra(TouristMainActivity.EXTRA_FILTER_MIN_PRICE, minPrice);
        if (maxPrice != null) intent.putExtra(TouristMainActivity.EXTRA_FILTER_MAX_PRICE, maxPrice);
        if (query != null) intent.putExtra(TouristMainActivity.EXTRA_FILTER_QUERY, query);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRecommendBundle(String payloadJson) {
        if (payloadJson != null && !payloadJson.isEmpty()) {
            try {
                JsonObject json = JsonParser.parseString(payloadJson).getAsJsonObject();
                if (json.has("slotId")) {
                    long slotId = json.get("slotId").getAsLong();
                    Intent intent = new Intent(this, SlotDetailActivity.class);
                    intent.putExtra(SlotDetailActivity.EXTRA_SLOT_ID, slotId);
                    startActivity(intent);
                    return;
                } else if (json.has("groupId")) {
                    long groupId = json.get("groupId").getAsLong();
                    Intent intent = new Intent(this, GroupDetailActivity.class);
                    intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, groupId);
                    startActivity(intent);
                    return;
                }
            } catch (Exception ignored) {}
        }

        // Fallback: navigate to explore
        onFilterSlots(payloadJson);
    }
}
