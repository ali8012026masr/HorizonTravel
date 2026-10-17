package com.example.horizontravel.ui.tourist;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.GroupMessage;
import com.example.horizontravel.data.model.MessageRequest;
import com.example.horizontravel.databinding.ActivityGroupChatBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends BaseActivity {

    public static final String EXTRA_GROUP_ID = "extra_group_id";
    public static final String EXTRA_GROUP_NAME = "extra_group_name";

    private ActivityGroupChatBinding binding;
    private long groupId;
    private GroupChatAdapter adapter;

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            loadMessages(false);
            pollHandler.postDelayed(this, 5000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);
        String groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);

        if (groupId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_group, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        if (groupName != null) {
            binding.toolbar.setTitle(groupName);
        }

        setupRecyclerView();
        setupListeners();
        loadMessages(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pollHandler.post(pollRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
    }

    @Override
    protected void onDestroy() {
        pollHandler.removeCallbacks(pollRunnable);
        super.onDestroy();
    }

    private void setupRecyclerView() {
        long myId = SessionManager.getInstance().getUserId();
        adapter = new GroupChatAdapter(myId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages(boolean showProgress) {
        if (showProgress) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService().getGroupMessages(groupId).enqueue(new ApiCallback<List<GroupMessage>>() {
            @Override
            public void onSuccess(List<GroupMessage> messages) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);

                if (messages == null || messages.isEmpty()) {
                    adapter.setMessages(new ArrayList<>());
                    binding.layoutEmptyChat.setVisibility(View.VISIBLE);
                } else {
                    adapter.setMessages(messages);
                    binding.layoutEmptyChat.setVisibility(View.GONE);
                    binding.rvMessages.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText() != null ? binding.etMessage.getText().toString().trim() : "";
        if (text.isEmpty()) return;

        long myId = SessionManager.getInstance().getUserId();
        MessageRequest request = new MessageRequest(myId, text);
        binding.btnSend.setEnabled(false);

        ApiClient.getApiService().sendGroupMessage(groupId, request).enqueue(new ApiCallback<GroupMessage>() {
            @Override
            public void onSuccess(GroupMessage msg) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnSend.setEnabled(true);
                binding.etMessage.setText("");
                loadMessages(false);
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.btnSend.setEnabled(true);
                Toast.makeText(GroupChatActivity.this,
                        message != null ? message : "Failed to send message",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
