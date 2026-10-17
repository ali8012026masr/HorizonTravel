package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Tourist;
import com.example.horizontravel.databinding.FragmentProfileBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.shared.ChangePasswordActivity;
import com.example.horizontravel.ui.shared.NotificationsActivity;
import com.example.horizontravel.util.SessionManager;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding> {

    @Override
    protected FragmentProfileBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupInitialSessionData();
        setupListeners();
        loadProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    private void setupInitialSessionData() {
        binding.tvProfileName.setText(SessionManager.getInstance().getFullName());
        binding.tvProfileEmail.setText(SessionManager.getInstance().getEmail());
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadProfile);

        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), EditProfileActivity.class));
        });

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NotificationsActivity.class));
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
        });

        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        binding.btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());
    }

    private void loadProfile() {
        long userId = SessionManager.getInstance().getUserId();
        if (userId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getTouristProfile(userId).enqueue(new ApiCallback<Tourist>() {
            @Override
            public void onSuccess(Tourist tourist) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (tourist != null) {
                    if (tourist.getFullName() != null) {
                        binding.tvProfileName.setText(tourist.getFullName());
                        SessionManager.getInstance().setFullName(tourist.getFullName());
                    }
                    if (tourist.getEmail() != null) {
                        binding.tvProfileEmail.setText(tourist.getEmail());
                    }
                    if (tourist.getPhone() != null && !tourist.getPhone().isEmpty()) {
                        binding.tvProfilePhone.setText(tourist.getPhone());
                        binding.tvProfilePhone.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvProfilePhone.setVisibility(View.GONE);
                    }
                    if (tourist.getAddress() != null && !tourist.getAddress().isEmpty()) {
                        binding.tvProfileAddress.setText(tourist.getAddress());
                        binding.tvProfileAddress.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvProfileAddress.setVisibility(View.GONE);
                    }
                    binding.tvLoyaltyPoints.setText(
                            getString(R.string.profile_loyalty_points, tourist.getLoyaltyPoints()));
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.msg_are_you_sure_you_want_to_s)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    SessionManager.getInstance().clear();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_account_title)
                .setMessage(R.string.confirm_delete_account_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteAccount() {
        long userId = SessionManager.getInstance().getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().deleteTouristAccount(userId).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.account_deleted_success, Toast.LENGTH_SHORT).show();
                SessionManager.getInstance().clear();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(),
                        message != null ? message : "Unable to delete account",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
