package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Group;
import com.example.horizontravel.databinding.ActivityGroupsBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class GroupsActivity extends BaseActivity {

    private ActivityGroupsBinding binding;
    private GroupAdapter adapter;
    private final List<Group> allGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        setupListeners();
        binding.etSearchGroups.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroups();
    }

    private void setupRecyclerView() {
        adapter = new GroupAdapter(group -> {
            if (group != null && group.getId() != null) {
                Intent intent = new Intent(this, GroupDetailActivity.class);
                intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, group.getId());
                startActivity(intent);
            }
        });
        binding.rvGroups.setLayoutManager(new LinearLayoutManager(this));
        binding.rvGroups.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadGroups);

        binding.btnCreateGroup.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateGroupActivity.class));
        });

        binding.btnJoinGroup.setOnClickListener(v -> {
            startActivity(new Intent(this, JoinGroupActivity.class));
        });
    }

    private void loadGroups() {
        long userId = SessionManager.getInstance().getUserId();
        if (userId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);

        ApiClient.getApiService().getGroupsForTourist(userId).enqueue(new ApiCallback<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (groups == null || groups.isEmpty()) {
                    allGroups.clear();
                    adapter.setGroups(new ArrayList<>());
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    allGroups.clear();
                    allGroups.addAll(groups);
                    applySearch(binding.etSearchGroups.getText());
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(GroupsActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applySearch(CharSequence query) {
        String term = query == null ? "" : query.toString().trim().toLowerCase(java.util.Locale.US);
        List<Group> filtered = new ArrayList<>();
        for (Group group : allGroups) {
            String name = group.getGroupName() == null ? "" : group.getGroupName().toLowerCase(java.util.Locale.US);
            String code = group.getJoinCode() == null ? "" : group.getJoinCode().toLowerCase(java.util.Locale.US);
            if (term.isEmpty() || name.contains(term) || code.contains(term)) {
                filtered.add(group);
            }
        }
        adapter.setGroups(filtered);
        binding.layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
