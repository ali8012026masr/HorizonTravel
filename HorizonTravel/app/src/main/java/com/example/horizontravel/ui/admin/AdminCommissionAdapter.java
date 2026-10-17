package com.example.horizontravel.ui.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Commission;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.databinding.ItemAdminCommissionBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class AdminCommissionAdapter extends RecyclerView.Adapter<AdminCommissionAdapter.CommissionViewHolder> {

    public interface OnCommissionSettleListener {
        void onSettle(Commission commission);
    }

    private final List<Commission> items = new ArrayList<>();
    private final OnCommissionSettleListener listener;

    public AdminCommissionAdapter(OnCommissionSettleListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Commission> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminCommissionBinding binding = ItemAdminCommissionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CommissionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommissionViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CommissionViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminCommissionBinding binding;

        CommissionViewHolder(ItemAdminCommissionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Commission commission) {
            Context context = binding.getRoot().getContext();

            String provider = commission.getProviderName() != null ? commission.getProviderName() : "Provider";
            String category = commission.getServiceCategory() != null ? Formatters.capitalizeEnum(commission.getServiceCategory()) : "Service";
            binding.tvCommissionProvider.setText(context.getString(R.string.commission_provider_format, provider, category));

            String status = commission.getSettlementStatus() != null ? commission.getSettlementStatus() : Constants.SettlementStatus.PENDING;
            binding.tvCommissionStatus.setText(status);

            boolean isPending = Constants.SettlementStatus.PENDING.equalsIgnoreCase(status);
            int colorRes = isPending ? R.color.badge_pending : R.color.badge_success;
            binding.tvCommissionStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, colorRes)));
            binding.tvCommissionStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_text));

            binding.tvCommissionAmount.setText(Formatters.formatCurrency(commission.getAmount()));
            binding.tvCommissionPercentage.setText(String.format(java.util.Locale.US, "Rate: %.1f%%", commission.getPercentage()));

            long bookingId = commission.getBookingId() != null ? commission.getBookingId() : 0;
            binding.tvCommissionBooking.setText(String.format(java.util.Locale.US, "Linked Booking: #%d", bookingId));

            String date = Formatters.formatDateTime(commission.getCalculatedDate());
            binding.tvCommissionDate.setText(date.isEmpty() ? "" : String.format(java.util.Locale.US, "Calculated: %s", date));

            if (isPending) {
                binding.btnSettleCommission.setVisibility(View.VISIBLE);
                binding.btnSettleCommission.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSettle(commission);
                    }
                });
            } else {
                binding.btnSettleCommission.setVisibility(View.GONE);
            }
        }
    }
}
