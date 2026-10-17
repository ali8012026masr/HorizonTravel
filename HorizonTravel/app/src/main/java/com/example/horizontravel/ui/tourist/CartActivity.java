package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.ServiceSlot;
import com.example.horizontravel.databinding.ActivityCartBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.CartManager;
import com.example.horizontravel.util.Formatters;

import java.util.List;

public class CartActivity extends BaseActivity implements CartManager.OnCartChangeListener {

    private ActivityCartBinding binding;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setSupportActionBar(binding.toolbar);

        setupRecyclerView();
        setupListeners();
        CartManager.getInstance().addListener(this);
        refreshCartUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartManager.getInstance().removeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem clearItem = menu.findItem(R.id.action_clear_cart);
        if (clearItem != null) {
            clearItem.setVisible(CartManager.getInstance().getCount() > 0);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            confirmClearCart();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmClearCart() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_clear_cart)
                .setMessage(R.string.msg_are_you_sure_you_want_to_e)
                .setPositiveButton(R.string.yes, (dialog, which) -> CartManager.getInstance().clear())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(slot -> {
            if (slot != null && slot.getId() != null) {
                CartManager.getInstance().removeItem(slot.getId());
            }
        });
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCartItems.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        binding.btnProceedCheckout.setOnClickListener(v -> {
            if (!com.example.horizontravel.util.SessionManager.getInstance().isLoggedIn()) {
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.dialog_sign_in_required_title)
                        .setMessage(R.string.dialog_sign_in_required_msg)
                        .setPositiveButton(R.string.dialog_sign_in_button, (dialog, which) -> {
                            startActivity(new Intent(this, com.example.horizontravel.ui.auth.LoginActivity.class));
                        })
                        .setNeutralButton(R.string.dialog_register_button, (dialog, which) -> {
                            startActivity(new Intent(this, com.example.horizontravel.ui.auth.RegisterChooserActivity.class));
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return;
            }
            startActivity(new Intent(this, CheckoutActivity.class));
        });

        binding.btnBrowseServices.setOnClickListener(v -> {
            finish();
        });
    }

    private void refreshCartUI() {
        List<ServiceSlot> items = CartManager.getInstance().getItems();
        cartAdapter.setItems(items);

        if (items.isEmpty()) {
            binding.rvCartItems.setVisibility(View.GONE);
            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
            binding.layoutBottomCheckout.setVisibility(View.GONE);
        } else {
            binding.rvCartItems.setVisibility(View.VISIBLE);
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.layoutBottomCheckout.setVisibility(View.VISIBLE);
            binding.tvCartTotal.setText(Formatters.formatCurrency(CartManager.getInstance().getTotal()));
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onCartChanged(int itemCount) {
        refreshCartUI();
    }
}
