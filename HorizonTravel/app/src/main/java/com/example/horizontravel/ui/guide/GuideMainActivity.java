package com.example.horizontravel.ui.guide;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.horizontravel.R;
import com.example.horizontravel.databinding.ActivityGuideMainBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.shared.ChangePasswordActivity;
import com.example.horizontravel.util.SessionManager;

public class GuideMainActivity extends BaseActivity {

    private ActivityGuideMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.horizontravel.util.SessionManager session = com.example.horizontravel.util.SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.TOUR_GUIDE.equalsIgnoreCase(session.getRole())) {
            startActivity(com.example.horizontravel.util.RoleRouter.shellIntentFor(this, session.getRole()));
            finish();
            return;
        }

        binding = ActivityGuideMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupShellWindowInsets(binding.getRoot(), binding.appBarLayout, binding.bottomNav, binding.fragmentContainer);

        setSupportActionBar(binding.toolbar);

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new GuideDashboardFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment fragment;

            if (itemId == R.id.nav_guide_dashboard) {
                binding.toolbar.setTitle(R.string.guide_nav_dashboard);
                fragment = new GuideDashboardFragment();
            } else if (itemId == R.id.nav_guide_availability) {
                binding.toolbar.setTitle(R.string.guide_nav_availability);
                fragment = new GuideAvailabilityFragment();
            } else if (itemId == R.id.nav_guide_requests) {
                binding.toolbar.setTitle(R.string.guide_nav_requests);
                fragment = new GuideRequestsFragment();
            } else if (itemId == R.id.nav_guide_profile) {
                binding.toolbar.setTitle(R.string.guide_nav_profile);
                fragment = new GuideProfileFragment();
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
        getMenuInflater().inflate(R.menu.menu_guide_main, menu);
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
