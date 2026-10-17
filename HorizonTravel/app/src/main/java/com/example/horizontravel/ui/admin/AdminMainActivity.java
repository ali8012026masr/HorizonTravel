package com.example.horizontravel.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.horizontravel.R;
import com.example.horizontravel.databinding.ActivityAdminMainBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.shared.ChangePasswordActivity;
import com.example.horizontravel.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AdminMainActivity extends BaseActivity {

    private ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.horizontravel.util.SessionManager session = com.example.horizontravel.util.SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.ADMIN.equalsIgnoreCase(session.getRole())) {
            startActivity(com.example.horizontravel.util.RoleRouter.shellIntentFor(this, session.getRole()));
            finish();
            return;
        }

        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupShellWindowInsets(binding.getRoot(), binding.appBarLayout, binding.bottomNav, binding.fragmentContainer);

        setSupportActionBar(binding.toolbar);

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new VerificationsFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment fragment;

            if (itemId == R.id.nav_admin_verifications) {
                binding.toolbar.setTitle(R.string.admin_nav_verifications);
                fragment = new VerificationsFragment();
            } else if (itemId == R.id.nav_admin_users) {
                binding.toolbar.setTitle(R.string.admin_nav_users);
                fragment = new UsersFragment();
            } else if (itemId == R.id.nav_admin_finance) {
                binding.toolbar.setTitle(R.string.admin_nav_finance);
                fragment = new FinanceFragment();
            } else if (itemId == R.id.nav_admin_reports) {
                binding.toolbar.setTitle(R.string.admin_nav_reports);
                fragment = new ReportsFragment();
            } else {
                return false;
            }

            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_main, menu);
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
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    SessionManager.getInstance().clear();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
