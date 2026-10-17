package com.example.horizontravel.ui.provider;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Commission;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.databinding.ItemCommissionBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class CommissionAdapter extends RecyclerView.Adapter<CommissionAdapter.CommissionViewHolder> {

    private final List<Commission> items = new ArrayList<>();

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
        ItemCommissionBinding binding = ItemCommissionBinding.inflate(
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

    static class CommissionViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommissionBinding binding;

        CommissionViewHolder(ItemCommissionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Commission commission) {
            Context context = binding.getRoot().getContext();

            long bookingId = commission.getBookingId() != null ? commission.getBookingId() : 0;
            binding.tvCommissionBooking.setText(context.getString(R.string.commission_item_format,
                    bookingId, commission.getPercentage()));

            String status = commission.getSettlementStatus() != null
                    ? commission.getSettlementStatus() : Constants.SettlementStatus.PENDING;
            binding.tvCommissionStatus.setText(status);

            int colorRes = Constants.SettlementStatus.SETTLED.equalsIgnoreCase(status)
                    ? R.color.badge_success : R.color.badge_pending;
            binding.tvCommissionStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, colorRes)));
            binding.tvCommissionStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_text));

            binding.tvCommissionAmount.setText(context.getString(R.string.commission_amount_format,
                    Formatters.formatCurrency(commission.getAmount())));

            String formattedDate = Formatters.formatDateTime(commission.getCalculatedDate());
            binding.tvCommissionDate.setText(formattedDate.isEmpty() ? "" : formattedDate);
        }
    }
}
