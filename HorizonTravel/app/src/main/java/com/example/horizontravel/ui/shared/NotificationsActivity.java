package com.example.horizontravel.ui.shared;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Notification;
import com.example.horizontravel.databinding.ActivityNotificationsBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

import java.util.List;
import java.util.ArrayList;

public class NotificationsActivity extends BaseActivity {
    private ActivityNotificationsBinding binding;
    private NotificationAdapter adapter;
    private final List<Notification> allNotifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        binding.etSearchNotifications.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.swipeRefresh.setOnRefreshListener(this::fetchNotifications);

        fetchNotifications();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter((notification, position) -> {
            if (!Boolean.TRUE.equals(notification.getRead())) {
                markAsRead(notification.getId(), position);
            }
        });
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void fetchNotifications() {
        long userId = SessionManager.getInstance().getUserId();
        if (userId <= 0) {
            binding.swipeRefresh.setRefreshing(false);
            showEmptyState(true);
            return;
        }

        if (!binding.swipeRefresh.isRefreshing() && adapter.getItemCount() == 0) {
            binding.pbLoading.setVisibility(View.VISIBLE);
        }

        ApiClient.getApiService().getNotifications(userId).enqueue(new ApiCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (!isSafe()) return;
                binding.pbLoading.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (notifications == null || notifications.isEmpty()) {
                    allNotifications.clear();
                    adapter.setItems(null);
                    showEmptyState(true);
                } else {
                    allNotifications.clear();
                    allNotifications.addAll(notifications);
                    applySearch(binding.etSearchNotifications.getText());
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.pbLoading.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                showToast(message);
                if (adapter.getItemCount() == 0) {
                    showEmptyState(true);
                }
            }
        });
    }

    private void applySearch(CharSequence query) {
        String term = query == null ? "" : query.toString().trim().toLowerCase(java.util.Locale.US);
        List<Notification> filtered = new ArrayList<>();
        for (Notification notification : allNotifications) {
            String searchable = ((notification.getMessage() == null ? "" : notification.getMessage()) + " "
                    + (notification.getType() == null ? "" : notification.getType())).toLowerCase(java.util.Locale.US);
            if (term.isEmpty() || searchable.contains(term)) {
                filtered.add(notification);
            }
        }
        adapter.setItems(filtered);
        showEmptyState(filtered.isEmpty());
    }

    private void markAsRead(Long notificationId, int position) {
        if (notificationId == null) return;

        ApiClient.getApiService().markNotificationAsRead(notificationId).enqueue(new ApiCallback<Notification>() {
            @Override
            public void onSuccess(Notification data) {
                if (!isSafe()) return;
                adapter.markItemRead(position);
            }

            @Override
            public void onError(String message, int statusCode) {
                // Silently keep current state on read error
            }
        });
    }

    private void markAllAsRead() {
        List<Notification> items = adapter.getItems();
        boolean hasUnread = false;
        for (Notification n : items) {
            if (!Boolean.TRUE.equals(n.getRead()) && n.getId() != null) {
                hasUnread = true;
                ApiClient.getApiService().markNotificationAsRead(n.getId()).enqueue(new ApiCallback<Notification>() {
                    @Override
                    public void onSuccess(Notification data) {}

                    @Override
                    public void onError(String message, int statusCode) {}
                });
            }
        }

        if (hasUnread) {
            adapter.markAllRead();
            showToast(getString(R.string.toast_all_notifications_marked_read));
        }
    }

    private void showEmptyState(boolean empty) {
        binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.rvNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_mark_all_read) {
            if (isSingleClick()) {
                markAllAsRead();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
