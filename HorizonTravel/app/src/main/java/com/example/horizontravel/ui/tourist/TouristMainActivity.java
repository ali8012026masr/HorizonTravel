package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.horizontravel.R;
import com.example.horizontravel.databinding.ActivityTouristMainBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.CartManager;

public class TouristMainActivity extends BaseActivity implements CartManager.OnCartChangeListener {

    public static final String EXTRA_TARGET_TAB = "extra_target_tab";
    public static final String EXTRA_FILTER_CATEGORY = "extra_filter_category";
    public static final String EXTRA_FILTER_MIN_PRICE = "extra_filter_min_price";
    public static final String EXTRA_FILTER_MAX_PRICE = "extra_filter_max_price";
    public static final String EXTRA_FILTER_QUERY = "extra_filter_query";

    private ActivityTouristMainBinding binding;

    private HomeFragment homeFragment;
    private ExploreFragment exploreFragment;
    private BookingsFragment bookingsFragment;
    private ProfileFragment profileFragment;

    private TextView tvCartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.horizontravel.util.SessionManager session = com.example.horizontravel.util.SessionManager.getInstance();
        if (!session.isLoggedIn() || !com.example.horizontravel.data.model.Constants.Role.TOURIST.equalsIgnoreCase(session.getRole())) {
            startActivity(com.example.horizontravel.util.RoleRouter.shellIntentFor(this, session.getRole()));
            finish();
            return;
        }

        binding = ActivityTouristMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupShellWindowInsets(binding.getRoot(), binding.appBarLayout, binding.bottomNav, binding.fragmentContainer, binding.fabAiConcierge);

        setSupportActionBar(binding.toolbar);

        binding.fabAiConcierge.setOnClickListener(v -> {
            if (isSingleClick()) {
                startActivity(new Intent(this, AiConciergeActivity.class));
            }
        });
        androidx.appcompat.widget.TooltipCompat.setTooltipText(binding.fabAiConcierge, getString(R.string.action_ai_concierge));

        initFragments();
        setupBottomNav();
        handleIntent(getIntent());

        CartManager.getInstance().addListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartManager.getInstance().removeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tourist_main, menu);

        // Setup Notifications Bell
        setupNotificationBell(menu);

        // Setup Cart Action
        MenuItem cartItem = menu.findItem(R.id.action_cart);
        if (cartItem != null) {
            View actionView = cartItem.getActionView();
            if (actionView != null) {
                com.example.horizontravel.databinding.ActionLayoutCartIconBinding cartBinding =
                        com.example.horizontravel.databinding.ActionLayoutCartIconBinding.bind(actionView);
                tvCartBadge = cartBinding.tvCartBadge;
                actionView.setOnClickListener(v -> {
                    startActivity(new Intent(this, CartActivity.class));
                });
                updateCartBadge();
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_password) {
            if (isSingleClick()) {
                startActivity(new Intent(this, com.example.horizontravel.ui.shared.ChangePasswordActivity.class));
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
                    com.example.horizontravel.util.SessionManager.getInstance().clear();
                    Intent intent = new Intent(this, com.example.horizontravel.ui.auth.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void updateCartBadge() {
        if (tvCartBadge == null) return;
        int count = CartManager.getInstance().getCount();
        if (count > 0) {
            tvCartBadge.setText(count > 99 ? "99+" : String.valueOf(count));
            tvCartBadge.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCartChanged(int itemCount) {
        updateCartBadge();
    }

    private void initFragments() {
        homeFragment = new HomeFragment();
        exploreFragment = new ExploreFragment();
        bookingsFragment = new BookingsFragment();
        profileFragment = new ProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, homeFragment, "HOME")
                .commit();
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                switchFragment(homeFragment, "Home");
                return true;
            } else if (id == R.id.nav_explore) {
                switchFragment(exploreFragment, "Explore");
                return true;
            } else if (id == R.id.nav_bookings) {
                switchFragment(bookingsFragment, "My Bookings");
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(profileFragment, "Profile");
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment fragment, String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void selectTab(int navItemId) {
        binding.bottomNav.setSelectedItemId(navItemId);
    }

    public void navigateToExplore(String category, Double minPrice, Double maxPrice, String query) {
        exploreFragment.applyFilters(category, minPrice, maxPrice, query);
        selectTab(R.id.nav_explore);
    }

    public void navigateToGuides() {
        selectTab(R.id.nav_explore);
        exploreFragment.showGuides();
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        if (intent.hasExtra(EXTRA_TARGET_TAB)) {
            int targetTab = intent.getIntExtra(EXTRA_TARGET_TAB, R.id.nav_home);
            if (targetTab == R.id.nav_explore) {
                String cat = intent.getStringExtra(EXTRA_FILTER_CATEGORY);
                Double min = intent.hasExtra(EXTRA_FILTER_MIN_PRICE) ? intent.getDoubleExtra(EXTRA_FILTER_MIN_PRICE, 0) : null;
                Double max = intent.hasExtra(EXTRA_FILTER_MAX_PRICE) ? intent.getDoubleExtra(EXTRA_FILTER_MAX_PRICE, 0) : null;
                String q = intent.getStringExtra(EXTRA_FILTER_QUERY);
                navigateToExplore(cat, min, max, q);
            } else {
                selectTab(targetTab);
            }
        }
    }
}
