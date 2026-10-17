package com.example.horizontravel.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.AdminUser;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.UpdateUserStatusRequest;
import com.example.horizontravel.databinding.FragmentUsersBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UsersFragment extends BaseFragment<FragmentUsersBinding> {

    private AdminUserAdapter adapter;
    private final List<AdminUser> rawUsers = new ArrayList<>();
    private String selectedRole = null;
    private String selectedStatus = null;
    private String currentSearchText = "";

    private static final String[] STATUS_OPTIONS = new String[] {
            Constants.UserStatus.ACTIVE,
            Constants.UserStatus.PENDING_VERIFICATION,
            Constants.UserStatus.SUSPENDED,
            Constants.UserStatus.REVOKED
    };

    @Override
    protected FragmentUsersBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentUsersBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilters();
        setupSearch();

        binding.swipeRefresh.setOnRefreshListener(this::loadUsers);

        loadUsers();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(user -> {
            if (isSingleClick() && user != null) {
                showStatusChangeDialog(user);
            }
        });
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUsers.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipRoleTourist) {
                selectedRole = Constants.Role.TOURIST;
            } else if (checkedId == R.id.chipRoleGuide) {
                selectedRole = Constants.Role.TOUR_GUIDE;
            } else if (checkedId == R.id.chipRoleProvider) {
                selectedRole = Constants.Role.SERVICE_PROVIDER;
            } else if (checkedId == R.id.chipRoleAdmin) {
                selectedRole = Constants.Role.ADMIN;
            } else {
                selectedRole = null;
            }
            loadUsers();
        });

        binding.chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipStatusActive) {
                selectedStatus = Constants.UserStatus.ACTIVE;
            } else if (checkedId == R.id.chipStatusPending) {
                selectedStatus = Constants.UserStatus.PENDING_VERIFICATION;
            } else if (checkedId == R.id.chipStatusSuspended) {
                selectedStatus = Constants.UserStatus.SUSPENDED;
            } else if (checkedId == R.id.chipStatusRevoked) {
                selectedStatus = Constants.UserStatus.REVOKED;
            } else {
                selectedStatus = null;
            }
            loadUsers();
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s != null ? s.toString().trim().toLowerCase(Locale.US) : "";
                applySearchFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmptyUsers.setVisibility(View.GONE);

        ApiClient.getApiService().getAdminUsers(selectedRole, selectedStatus).enqueue(new ApiCallback<List<AdminUser>>() {
            @Override
            public void onSuccess(List<AdminUser> users) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                rawUsers.clear();
                if (users != null) {
                    rawUsers.addAll(users);
                }
                applySearchFilter();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                showError(message);
                updateEmptyState(adapter.getItemCount() == 0);
            }
        });
    }

    private void applySearchFilter() {
        List<AdminUser> filtered = new ArrayList<>();
        for (AdminUser u : rawUsers) {
            if (currentSearchText.isEmpty()) {
                filtered.add(u);
            } else {
                String name = u.getFullName() != null ? u.getFullName().toLowerCase(Locale.US) : "";
                String email = u.getEmail() != null ? u.getEmail().toLowerCase(Locale.US) : "";
                if (name.contains(currentSearchText) || email.contains(currentSearchText)) {
                    filtered.add(u);
                }
            }
        }
        adapter.setItems(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.layoutEmptyUsers.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showStatusChangeDialog(AdminUser user) {
        int initialIndex = 0;
        String curStatus = user.getStatus();
        for (int i = 0; i < STATUS_OPTIONS.length; i++) {
            if (STATUS_OPTIONS[i].equalsIgnoreCase(curStatus)) {
                initialIndex = i;
                break;
            }
        }

        final int[] selectedIndex = {initialIndex};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.status_change_title, user.getFullName()))
                .setSingleChoiceItems(STATUS_OPTIONS, initialIndex, (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String targetStatus = STATUS_OPTIONS[selectedIndex[0]];
                    if (targetStatus.equalsIgnoreCase(curStatus)) {
                        return; // No change
                    }
                    if (Constants.UserStatus.SUSPENDED.equalsIgnoreCase(targetStatus)) {
                        confirmSuspend(user, targetStatus);
                    } else if (Constants.UserStatus.REVOKED.equalsIgnoreCase(targetStatus)) {
                        confirmRevoke(user, targetStatus);
                    } else {
                        executeStatusUpdate(user.getId(), targetStatus);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmSuspend(AdminUser user, String targetStatus) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_suspend_title)
                .setMessage(getString(R.string.confirm_suspend_msg, user.getFullName()))
                .setPositiveButton(R.string.filter_status_suspended, (dialog, which) -> {
                    executeStatusUpdate(user.getId(), targetStatus);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmRevoke(AdminUser user, String targetStatus) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_revoke_title)
                .setMessage(getString(R.string.confirm_revoke_msg, user.getFullName()))
                .setPositiveButton(R.string.filter_status_revoked, (dialog, which) -> {
                    executeStatusUpdate(user.getId(), targetStatus);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executeStatusUpdate(long userId, String newStatus) {
        binding.progressBar.setVisibility(View.VISIBLE);
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(newStatus);

        ApiClient.getApiService().updateUserStatus(userId, request).enqueue(new ApiCallback<AdminUser>() {
            @Override
            public void onSuccess(AdminUser updated) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                showToast(getString(R.string.user_status_updated));
                loadUsers();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                showError(message);
            }
        });
    }
}
