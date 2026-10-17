package com.example.horizontravel.ui.guide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.GuideBooking;
import com.example.horizontravel.data.model.UpdateGuideBookingStatusRequest;
import com.example.horizontravel.databinding.FragmentGuideRequestsBinding;
import com.example.horizontravel.ui.BaseFragment;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class GuideRequestsFragment extends BaseFragment<FragmentGuideRequestsBinding> {

    private GuideIncomingBookingAdapter adapter;
    private long guideId;
    private final List<GuideBooking> allBookings = new ArrayList<>();
    private String selectedFilter = "ALL";

    @Override
    protected FragmentGuideRequestsBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGuideRequestsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        guideId = SessionManager.getInstance().getUserId();

        setupRecyclerView();
        setupFilters();
        setupListeners();
        loadRequests();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRequests();
    }

    private void setupRecyclerView() {
        adapter = new GuideIncomingBookingAdapter(new GuideIncomingBookingAdapter.OnBookingActionListener() {
            @Override
            public void onAccept(GuideBooking booking) {
                acceptRequest(booking);
            }

            @Override
            public void onDecline(GuideBooking booking) {
                confirmDeclineRequest(booking);
            }

            @Override
            public void onMarkCompleted(GuideBooking booking) {
                markRequestCompleted(booking);
            }

            @Override
            public void onMarkPaymentReceived(GuideBooking booking) {
                confirmPaymentReceived(booking);
            }

            @Override
            public void onItemClick(GuideBooking booking) {}
        }, true);

        binding.rvRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRequests.setAdapter(adapter);
    }

    private void setupFilters() {
        binding.chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipRequested) {
                selectedFilter = Constants.GuideBookingStatus.REQUESTED;
            } else if (checkedId == R.id.chipAccepted) {
                selectedFilter = Constants.GuideBookingStatus.ACCEPTED;
            } else if (checkedId == R.id.chipCompleted) {
                selectedFilter = Constants.GuideBookingStatus.COMPLETED;
            } else if (checkedId == R.id.chipDeclined) {
                selectedFilter = Constants.GuideBookingStatus.DECLINED;
            } else {
                selectedFilter = "ALL";
            }
            applyFilterAndDisplay();
        });
    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(this::loadRequests);
        binding.btnRetry.setOnClickListener(v -> loadRequests());
    }

    private void loadRequests() {
        if (!isSafe() || guideId <= 0) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        ApiClient.getApiService().getGuideBookingsForGuide(guideId).enqueue(new ApiCallback<List<GuideBooking>>() {
            @Override
            public void onSuccess(List<GuideBooking> list) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                allBookings.clear();
                if (list != null) {
                    allBookings.addAll(list);
                }
                applyFilterAndDisplay();
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

    private void applyFilterAndDisplay() {
        List<GuideBooking> filtered = new ArrayList<>();
        for (GuideBooking b : allBookings) {
            if ("ALL".equalsIgnoreCase(selectedFilter) ||
                    (b.getStatus() != null && b.getStatus().equalsIgnoreCase(selectedFilter))) {
                filtered.add(b);
            }
        }

        adapter.setBookings(filtered);
        binding.layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void acceptRequest(GuideBooking booking) {
        if (!isSafe() || booking == null || booking.getId() == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        UpdateGuideBookingStatusRequest req = new UpdateGuideBookingStatusRequest(Constants.GuideBookingStatus.ACCEPTED);

        ApiClient.getApiService().updateGuideBookingStatus(booking.getId(), req).enqueue(new ApiCallback<GuideBooking>() {
            @Override
            public void onSuccess(GuideBooking updated) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.request_accepted_success, Toast.LENGTH_SHORT).show();
                loadRequests();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message != null ? message : "Unable to accept request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeclineRequest(GuideBooking booking) {
        if (!isSafe() || booking == null || booking.getId() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_decline_request_title)
                .setMessage(R.string.confirm_decline_request_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> declineRequest(booking))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void declineRequest(GuideBooking booking) {
        binding.progressBar.setVisibility(View.VISIBLE);
        UpdateGuideBookingStatusRequest req = new UpdateGuideBookingStatusRequest(Constants.GuideBookingStatus.DECLINED);

        ApiClient.getApiService().updateGuideBookingStatus(booking.getId(), req).enqueue(new ApiCallback<GuideBooking>() {
            @Override
            public void onSuccess(GuideBooking updated) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.request_declined_success, Toast.LENGTH_SHORT).show();
                loadRequests();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message != null ? message : "Unable to decline request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markRequestCompleted(GuideBooking booking) {
        if (!isSafe() || booking == null || booking.getId() == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        UpdateGuideBookingStatusRequest req = new UpdateGuideBookingStatusRequest(Constants.GuideBookingStatus.COMPLETED);

        ApiClient.getApiService().updateGuideBookingStatus(booking.getId(), req).enqueue(new ApiCallback<GuideBooking>() {
            @Override
            public void onSuccess(GuideBooking updated) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.request_completed_success, Toast.LENGTH_SHORT).show();
                loadRequests();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message != null ? message : "Unable to complete request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmPaymentReceived(GuideBooking booking) {
        if (!isSafe() || booking == null || booking.getId() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_payment_received_title)
                .setMessage(getString(R.string.confirm_payment_received_msg, Formatters.formatCurrency(booking.getAgreedPrice())))
                .setPositiveButton(R.string.yes, (dialog, which) -> markPaymentReceived(booking))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void markPaymentReceived(GuideBooking booking) {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().markGuideBookingPaymentReceived(booking.getId()).enqueue(new ApiCallback<GuideBooking>() {
            @Override
            public void onSuccess(GuideBooking updated) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), R.string.payment_received_success, Toast.LENGTH_SHORT).show();
                loadRequests();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (!isSafe()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message != null ? message : "Unable to mark payment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
