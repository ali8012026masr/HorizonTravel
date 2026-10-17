package com.example.horizontravel.ui.guide;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.GuideAvailability;
import com.example.horizontravel.databinding.FragmentGuideAvailabilityBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class GuideAvailabilityFragment extends BaseFragment<FragmentGuideAvailabilityBinding> {

    private GuideAvailabilityAdapter adapter;
    private long guideId;
    private final List<GuideAvailability> allAvailability = new ArrayList<>();

    @Override
    protected FragmentGuideAvailabilityBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGuideAvailabilityBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        guideId = SessionManager.getInstance().getUserId();

        setupRecyclerView();
        setupListeners();
        binding.etSearchAvailability.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applySearch(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        loadAvailability();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAvailability();
    }

    private void setupRecyclerView() {
        adapter = new GuideAvailabilityAdapter(item -> {
            if (item != null && item.getId() != null) {
                Intent intent = new Intent(requireContext(), EditAvailabilityActivity.class);
                intent.putExtra(EditAvailabilityActivity.EXTRA_AVAILABILITY_ID, item.getId());
                startActivity(intent);
            }
        });

        binding.rvAvailability.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAvailability.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadAvailability);

        binding.btnRetry.setOnClickListener(v -> loadAvailability());

        binding.fabAddAvailability.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateAvailabilityActivity.class);
            startActivity(intent);
        });
    }

    private void loadAvailability() {
        if (!isSafe() || guideId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        ApiClient.getApiService().getGuideAvailabilityByGuideId(guideId).enqueue(new ApiCallback<List<GuideAvailability>>() {
            @Override
            public void onSuccess(List<GuideAvailability> list) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                if (list == null || list.isEmpty()) {
                    allAvailability.clear();
                    adapter.setItems(new ArrayList<>());
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    allAvailability.clear();
                    allAvailability.addAll(list);
                    applySearch(binding.etSearchAvailability.getText());
                }
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

    private void applySearch(CharSequence query) {
        String term = query == null ? "" : query.toString().trim().toLowerCase(java.util.Locale.US);
        List<GuideAvailability> filtered = new ArrayList<>();
        for (GuideAvailability item : allAvailability) {
            String text = ((item.getLocation() == null ? "" : item.getLocation()) + " "
                    + (item.getStatus() == null ? "" : item.getStatus()) + " "
                    + (item.getNotes() == null ? "" : item.getNotes())).toLowerCase(java.util.Locale.US);
            if (term.isEmpty() || text.contains(term)) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
        binding.layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
