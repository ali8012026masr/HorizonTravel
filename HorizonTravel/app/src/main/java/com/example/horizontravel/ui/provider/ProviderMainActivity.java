package com.example.horizontravel.ui.provider;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.horizontravel.R;
import com.example.horizontravel.databinding.ActivityProviderMainBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.shared.ChangePasswordActivity;
import com.example.horizontravel.util.SessionManager;

public class ProviderMainActivity extends BaseActivity {

    private ActivityProviderMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.horizontravel.util.SessionManager session = com.example.horizontravel.util.SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.SERVICE_PROVIDER.equalsIgnoreCase(session.getRole())) {
            startActivity(com.example.horizontravel.util.RoleRouter.shellIntentFor(this, session.getRole()));
            finish();
            return;
        }

        binding = ActivityProviderMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupShellWindowInsets(binding.getRoot(), binding.appBarLayout, binding.bottomNav, binding.fragmentContainer);

        setSupportActionBar(binding.toolbar);

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new ProviderDashboardFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment fragment;

            if (itemId == R.id.nav_provider_dashboard) {
                binding.toolbar.setTitle(R.string.provider_nav_dashboard);
                fragment = new ProviderDashboardFragment();
            } else if (itemId == R.id.nav_provider_slots) {
                binding.toolbar.setTitle(R.string.provider_nav_slots);
                fragment = new ProviderSlotsFragment();
            } else if (itemId == R.id.nav_provider_ledger) {
                binding.toolbar.setTitle(R.string.provider_nav_ledger);
                fragment = new ProviderLedgerFragment();
            } else if (itemId == R.id.nav_provider_profile) {
                binding.toolbar.setTitle(R.string.provider_nav_profile);
                fragment = new ProviderProfileFragment();
            } else {
                return false;
            }

            loadFragment(fragment);
            return true;
        });
    }

    public void switchToTab(int navItemId) {
        if (binding != null && binding.bottomNav != null) {
            binding.bottomNav.setSelectedItemId(navItemId);
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_provider_main, menu);
        setupNotificationBell(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_password) {
            if (isSingleClick()) {
                startActivity(new Intent(this, ChangePasswordActivity.class));
            }
            return true;
        } else if (id == R.id.action_logout) {
            if (isSingleClick()) {
                confirmLogout();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    SessionManager.getInstance().clear();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
