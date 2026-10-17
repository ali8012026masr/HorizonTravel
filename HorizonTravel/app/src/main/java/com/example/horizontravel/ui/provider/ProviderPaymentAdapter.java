package com.example.horizontravel.ui.provider;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.horizontravel.R;
import com.example.horizontravel.data.model.Constants;
import com.example.horizontravel.data.model.Payment;
import com.example.horizontravel.data.model.Commission;
import com.example.horizontravel.databinding.ItemProviderPaymentBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class ProviderPaymentAdapter extends RecyclerView.Adapter<ProviderPaymentAdapter.PaymentViewHolder> {

    private final List<Payment> items = new ArrayList<>();
    private final List<Commission> commissions = new ArrayList<>();

    public void setItems(List<Payment> newItems) {
        setItems(newItems, null);
    }

    public void setItems(List<Payment> newItems, List<Commission> newCommissions) {
        items.clear();
        commissions.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        if (newCommissions != null) {
            commissions.addAll(newCommissions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProviderPaymentBinding binding = ItemProviderPaymentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PaymentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        holder.bind(items.get(position), commissions);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        private final ItemProviderPaymentBinding binding;

        PaymentViewHolder(ItemProviderPaymentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Payment payment, List<Commission> commissions) {
            Context context = binding.getRoot().getContext();

            binding.tvPaymentTxId.setText(String.format(java.util.Locale.US, "Payment #%d", payment.getId()));

            String status = payment.getStatus() != null ? payment.getStatus() : Constants.PaymentTxStatus.PENDING;
            binding.tvPaymentStatus.setText(status);

            int colorRes = Constants.PaymentTxStatus.SUCCESS.equalsIgnoreCase(status)
                    ? R.color.badge_success
                    : (Constants.PaymentTxStatus.FAILED.equalsIgnoreCase(status)
                    ? R.color.badge_error
                    : R.color.badge_pending);
            binding.tvPaymentStatus.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(context, colorRes)));
            binding.tvPaymentStatus.setTextColor(ContextCompat.getColor(context, R.color.badge_text));

            String method = payment.getMethod() != null ? Formatters.capitalizeEnum(payment.getMethod()) : "Unknown";
            binding.tvPaymentMethod.setText(String.format(java.util.Locale.US, "Method: %s", method));

            double commissionAmount = 0;
            for (Commission commission : commissions) {
                if (commission.getBookingId() != null
                        && (commission.getBookingId().equals(payment.getBookingId())
                        || commission.getBookingId().equals(payment.getGuideBookingId()))) {
                    commissionAmount += commission.getAmount();
                }
            }
            double netAmount = Math.max(0, payment.getAmount() - commissionAmount);
            binding.tvPaymentAmount.setText(binding.getRoot().getContext().getString(
                    R.string.provider_payment_net_format, Formatters.formatCurrency(netAmount)));
            binding.tvPaymentBreakdown.setText(binding.getRoot().getContext().getString(
                    R.string.provider_payment_breakdown_format,
                    Formatters.formatCurrency(payment.getAmount()),
                    Formatters.formatCurrency(commissionAmount)));

            String date = Formatters.formatDateTime(payment.getTransactionDate());
            binding.tvPaymentDate.setText(String.format(java.util.Locale.US, "Date: %s", date.isEmpty() ? "N/A" : date));
        }
    }
}
