package com.example.horizontravel.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

public abstract class BaseFragment<VB extends ViewBinding> extends Fragment {
    protected VB binding;
    private long lastClickTime = 0;
    private static final long CLICK_DEBOUNCE_MS = 800L;

    protected abstract VB inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflateBinding(inflater, container);
        return binding != null ? binding.getRoot() : null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public boolean isSafe() {
        return isAdded() && getContext() != null && !isDetached() && getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed();
    }

    public void showToast(String message) {
        if (isSafe() && message != null && !message.trim().isEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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

    public void showLoading(boolean loading) {
        // Optional override in subclasses for uniform loading handling
    }

    public void showError(String message) {
        if (isSafe() && message != null) {
            showToast(message);
        }
    }

    public void showEmpty(boolean empty) {
        // Optional override in subclasses
    }
}
