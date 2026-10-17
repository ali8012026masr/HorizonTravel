package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Guide;
import com.example.horizontravel.databinding.FragmentGuidesBinding;
import com.example.horizontravel.ui.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class GuidesFragment extends BaseFragment<FragmentGuidesBinding> {

    private GuideAdapter guideAdapter;
    private final List<Guide> allGuides = new ArrayList<>();

    @Override
    protected FragmentGuidesBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGuidesBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupListeners();
        loadGuides();
    }

    private void setupRecyclerView() {
        guideAdapter = new GuideAdapter(guide -> {
            if (guide != null && guide.getId() != null) {
                Intent intent = new Intent(requireContext(), GuideDetailActivity.class);
                intent.putExtra(GuideDetailActivity.EXTRA_GUIDE_ID, guide.getId());
                startActivity(intent);
            }
        });
        binding.rvGuides.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGuides.setAdapter(guideAdapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadGuides);

        binding.btnSearch.setOnClickListener(v -> filterAndDisplay());

        binding.switchAvailableOnly.setOnCheckedChangeListener((buttonView, isChecked) -> loadGuides());

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterAndDisplay();
                return true;
            }
            return false;
        });

        binding.btnRetry.setOnClickListener(v -> loadGuides());
    }

    private void loadGuides() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        Boolean availableOnly = binding.switchAvailableOnly.isChecked() ? true : null;

        ApiClient.getApiService().getGuides(availableOnly).enqueue(new ApiCallback<List<Guide>>() {
            @Override
            public void onSuccess(List<Guide> guides) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                allGuides.clear();
                if (guides != null) {
                    allGuides.addAll(guides);
                }
                filterAndDisplay();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_generic));
            }
        });
    }

    private void filterAndDisplay() {
        String query = "";
        if (binding.etSearch.getText() != null) {
            query = binding.etSearch.getText().toString().trim().toLowerCase();
        }

        List<Guide> filtered = new ArrayList<>();
        for (Guide g : allGuides) {
            if (query.isEmpty()) {
                filtered.add(g);
            } else {
                boolean matchesName = g.getFullName() != null && g.getFullName().toLowerCase().contains(query);
                boolean matchesLoc = g.getLocation() != null && g.getLocation().toLowerCase().contains(query);
                boolean matchesLang = false;
                if (g.getLanguages() != null) {
                    for (String l : g.getLanguages()) {
                        if (l != null && l.toLowerCase().contains(query)) {
                            matchesLang = true;
                            break;
                        }
                    }
                }
                if (matchesName || matchesLoc || matchesLang) {
                    filtered.add(g);
                }
            }
        }

        guideAdapter.setGuides(filtered);
        binding.layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
