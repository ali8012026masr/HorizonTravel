package com.example.horizontravel.ui.tourist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.horizontravel.R;
import com.example.horizontravel.databinding.FragmentExploreBinding;
import com.example.horizontravel.ui.BaseFragment;

public class ExploreFragment extends BaseFragment<FragmentExploreBinding> {
    private SlotsFragment slotsFragment;
    private GuidesFragment guidesFragment;
    private int selectedTab;
    private String pendingCategory;
    private Double pendingMinPrice;
    private Double pendingMaxPrice;
    private String pendingQuery;

    @Override
    protected FragmentExploreBinding inflateBinding(@NonNull LayoutInflater inflater,
                                                     @Nullable ViewGroup container) {
        return FragmentExploreBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        slotsFragment = new SlotsFragment();
        guidesFragment = new GuidesFragment();
        binding.tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                showSelectedFragment();
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }
        });
        binding.tabLayout.getTabAt(selectedTab).select();
        showSelectedFragment();
        if (pendingCategory != null || pendingMinPrice != null || pendingMaxPrice != null || pendingQuery != null) {
            slotsFragment.applyFilters(pendingCategory, pendingMinPrice, pendingMaxPrice, pendingQuery);
            pendingCategory = null;
            pendingMinPrice = null;
            pendingMaxPrice = null;
            pendingQuery = null;
        }
    }

    private void showSelectedFragment() {
        Fragment fragment = selectedTab == 1 ? guidesFragment : slotsFragment;
        getChildFragmentManager().beginTransaction()
                .replace(R.id.exploreContent, fragment)
                .commit();
    }

    public void showGuides() {
        selectedTab = 1;
        if (binding != null) {
            binding.tabLayout.getTabAt(1).select();
        }
    }

    public void applyFilters(String category, Double minPrice, Double maxPrice, String query) {
        pendingCategory = category;
        pendingMinPrice = minPrice;
        pendingMaxPrice = maxPrice;
        pendingQuery = query;
        selectedTab = 0;
        if (slotsFragment == null) {
            return;
        }
        slotsFragment.applyFilters(category, minPrice, maxPrice, query);
        if (binding != null) {
            binding.tabLayout.getTabAt(0).select();
            showSelectedFragment();
        }
    }
}
