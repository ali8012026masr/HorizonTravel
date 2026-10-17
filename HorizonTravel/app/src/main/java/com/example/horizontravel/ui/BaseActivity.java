package com.example.horizontravel.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.api.AuthInterceptor;
import com.example.horizontravel.data.model.CountResponse;
import com.example.horizontravel.databinding.ActionLayoutNotificationBellBinding;
import com.example.horizontravel.ui.auth.LoginActivity;
import com.example.horizontravel.ui.shared.NotificationsActivity;
import com.example.horizontravel.util.SessionManager;

public abstract class BaseActivity extends AppCompatActivity {
    private long lastClickTime = 0;
    private static final long CLICK_DEBOUNCE_MS = 800L;
    private TextView notificationBadgeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        boolean isLightTheme = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK) != android.content.res.Configuration.UI_MODE_NIGHT_YES;
        controller.setAppearanceLightStatusBars(isLightTheme);
        controller.setAppearanceLightNavigationBars(isLightTheme);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applyAutoWindowInsets(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View root = findViewHelper(getWindow().getDecorView(), android.R.id.content);
        if (root instanceof ViewGroup && ((ViewGroup) root).getChildCount() > 0) {
            applyAutoWindowInsets(((ViewGroup) root).getChildAt(0));
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        applyAutoWindowInsets(view);
    }

    protected void applyAutoWindowInsets(View view) {
        if (view == null) return;

        View appBar = findViewHelper(view, R.id.appBarLayout);
        if (appBar == null) {
            appBar = findViewHelper(view, R.id.toolbar);
        }
        View bottomNav = findViewHelper(view, R.id.bottomNav);
        View fragmentContainer = findViewHelper(view, R.id.fragmentContainer);
        View fab = findViewHelper(view, R.id.fabAiConcierge);

        if (bottomNav != null && fragmentContainer != null) {
            setupShellWindowInsets(view, appBar, bottomNav, fragmentContainer, fab);
        } else if (appBar != null) {
            setupToolbarWindowInsets(view, appBar, null);
        } else {
            applySystemBarInsets(view);
        }
    }

    private View findViewHelper(View root, int id) {
        if (root == null) return null;
        if (root.getId() == id) return root;
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = findViewHelper(vg.getChildAt(i), id);
                if (child != null) return child;
            }
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AuthInterceptor.setSessionExpiredListener(this::handleGlobalSessionExpired);
        if (notificationBadgeView != null) {
            refreshNotificationBadge();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AuthInterceptor.setSessionExpiredListener(null);
    }

    private void handleGlobalSessionExpired() {
        runOnUiThread(() -> {
            if (isSafe()) {
                SessionManager.getInstance().clear();
                Toast.makeText(this, R.string.error_session_expired, Toast.LENGTH_LONG).show();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
        });
    }

    public void setupNotificationBell(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_notifications);
        if (item != null) {
            View actionView = item.getActionView();
            if (actionView != null) {
                ActionLayoutNotificationBellBinding bellBinding = ActionLayoutNotificationBellBinding.bind(actionView);
                notificationBadgeView = bellBinding.tvNotificationBadge;
                actionView.setOnClickListener(v -> {
                    if (isSingleClick()) {
                        startActivity(new Intent(this, NotificationsActivity.class));
                    }
                });
            }
        }
        refreshNotificationBadge();
    }

    public void refreshNotificationBadge() {
        if (!isSafe()) return;
        SessionManager session = SessionManager.getInstance();
        if (!session.isLoggedIn()) return;
        long userId = session.getUserId();
        if (userId <= 0) return;

        ApiClient.getApiService().getUnreadNotificationCount(userId).enqueue(new ApiCallback<CountResponse>() {
            @Override
            public void onSuccess(CountResponse response) {
                if (!isSafe() || notificationBadgeView == null) return;
                int count = response != null ? response.getCount() : 0;
                if (count > 0) {
                    notificationBadgeView.setText(count > 99 ? "99+" : String.valueOf(count));
                    notificationBadgeView.setVisibility(View.VISIBLE);
                } else {
                    notificationBadgeView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message, int statusCode) {
                // Silently ignore badge errors
            }
        });
    }

    public boolean isSafe() {
        return !isFinishing() && !isDestroyed();
    }

    public void showToast(String message) {
        if (isSafe() && message != null && !message.trim().isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isSingleClick() {
        long current = SystemClock.elapsedRealtime();
        if (current - lastClickTime < CLICK_DEBOUNCE_MS) {
            return false;
        }
        lastClickTime = current;
        return true;
    }

    /**
     * Applies system window insets (status bar at top, navigation bar at bottom)
     * as padding to the given root view. Suitable for screens without a separate AppBarLayout.
     */
    public void applySystemBarInsets(View view) {
        if (view == null) return;
        final int initialLeft = view.getPaddingLeft();
        final int initialTop = view.getPaddingTop();
        final int initialRight = view.getPaddingRight();
        final int initialBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(
                    initialLeft + insets.left,
                    initialTop + insets.top,
                    initialRight + insets.right,
                    initialBottom + insets.bottom
            );
            return windowInsets;
        });
    }

    /**
     * Applies system window insets to role main shell activities (Guide, Tourist, Provider, Admin).
     * Top status bar inset is applied to AppBarLayout so toolbar and menu icons are safely below status bar.
     * Bottom navigation bar inset is applied to BottomNavigationView so tabs are safely above navigation bar.
     * Bottom margin of the fragment container is updated so fragments are never obscured by bottomNav.
     */
    public void setupShellWindowInsets(
            View rootView,
            View appBarLayout,
            View bottomNav,
            View fragmentContainer
    ) {
        setupShellWindowInsets(rootView, appBarLayout, bottomNav, fragmentContainer, null);
    }

    public void setupShellWindowInsets(
            View rootView,
            View appBarLayout,
            View bottomNav,
            View fragmentContainer,
            @Nullable View floatingActionButton
    ) {
        if (rootView == null) return;
        final int initialTop = appBarLayout != null ? appBarLayout.getPaddingTop() : 0;
        final int initialBottom = bottomNav != null ? bottomNav.getPaddingBottom() : 0;

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            if (appBarLayout != null) {
                appBarLayout.setPadding(
                        appBarLayout.getPaddingLeft(),
                        initialTop + insets.top,
                        appBarLayout.getPaddingRight(),
                        appBarLayout.getPaddingBottom()
                );
            }
            if (bottomNav != null) {
                bottomNav.setPadding(
                        bottomNav.getPaddingLeft(),
                        bottomNav.getPaddingTop(),
                        bottomNav.getPaddingRight(),
                        initialBottom + insets.bottom
                );
            }
            if (fragmentContainer != null) {
                ViewGroup.LayoutParams lp = fragmentContainer.getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                    int baseNavHeight = (int) (80 * getResources().getDisplayMetrics().density);
                    mlp.bottomMargin = baseNavHeight + insets.bottom;
                    fragmentContainer.setLayoutParams(mlp);
                }
            }
            if (floatingActionButton != null) {
                ViewGroup.LayoutParams lp = floatingActionButton.getLayoutParams();
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                    int baseFabBottom = (int) (16 * getResources().getDisplayMetrics().density);
                    int navigationHeight = bottomNav != null ? bottomNav.getHeight() : 0;
                    mlp.bottomMargin = baseFabBottom + navigationHeight;
                    floatingActionButton.setLayoutParams(mlp);
                }
            }
            rootView.post(() -> {
                if (fragmentContainer != null) {
                    ViewGroup.LayoutParams lp = fragmentContainer.getLayoutParams();
                    if (lp instanceof ViewGroup.MarginLayoutParams && appBarLayout != null && bottomNav != null) {
                        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                        mlp.bottomMargin = bottomNav.getHeight();
                        fragmentContainer.setLayoutParams(mlp);
                    }
                }
            });
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(rootView);
    }

    /**
     * Applies system window insets for screens with an AppBarLayout.
     * Top status bar inset is applied to AppBarLayout so toolbar and action icons are below status bar.
     * Bottom navigation bar inset is applied to bottomView (or rootView if bottomView is null) so bottom content/actions are above nav bar.
     */
    public void setupToolbarWindowInsets(View rootView, View appBarLayout, @Nullable View bottomView) {
        if (rootView == null) return;
        final int initialTop = appBarLayout != null ? appBarLayout.getPaddingTop() : 0;
        final int initialBottom = bottomView != null ? bottomView.getPaddingBottom() : rootView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            if (appBarLayout != null) {
                appBarLayout.setPadding(
                        appBarLayout.getPaddingLeft(),
                        initialTop + insets.top,
                        appBarLayout.getPaddingRight(),
                        appBarLayout.getPaddingBottom()
                );
            }
            if (bottomView != null) {
                bottomView.setPadding(
                        bottomView.getPaddingLeft(),
                        bottomView.getPaddingTop(),
                        bottomView.getPaddingRight(),
                        initialBottom + insets.bottom
                );
            } else {
                v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        initialBottom + insets.bottom
                );
            }
            return windowInsets;
        });
    }

    public void setupToolbarWindowInsets(View rootView, View appBarLayout) {
        setupToolbarWindowInsets(rootView, appBarLayout, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme) {
            showThemeSelectionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showThemeSelectionDialog() {
        int currentMode = SessionManager.getInstance().getThemeMode();
        int checkedItem = 2; // Default to system
        if (currentMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO) {
            checkedItem = 0;
        } else if (currentMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
            checkedItem = 1;
        }

        String[] options = {getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_system)};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.choose_theme)
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    int newMode;
                    if (which == 0) {
                        newMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
                    } else if (which == 1) {
                        newMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
                    } else {
                        newMode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    }
                    
                    if (newMode != currentMode) {
                        SessionManager.getInstance().setThemeMode(newMode);
                        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(newMode);
                    }
                    dialog.dismiss();
                })
                .show();
    }
}
