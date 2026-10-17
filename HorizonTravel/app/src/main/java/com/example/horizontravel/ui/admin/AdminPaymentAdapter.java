package com.example.horizontravel.ui.admin;

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
import com.example.horizontravel.databinding.ItemAdminPaymentBinding;
import com.example.horizontravel.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class AdminPaymentAdapter extends RecyclerView.Adapter<AdminPaymentAdapter.PaymentViewHolder> {

    private final List<Payment> items = new ArrayList<>();

    public void setItems(List<Payment> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminPaymentBinding binding = ItemAdminPaymentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PaymentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminPaymentBinding binding;

        PaymentViewHolder(ItemAdminPaymentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Payment payment) {
            Context context = binding.getRoot().getContext();

            binding.tvPaymentTitle.setText(context.getString(R.string.payment_card_format,
                    payment.getId(), Formatters.formatCurrency(payment.getAmount())));

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

            String method = payment.getMethod() != null ? Formatters.capitalizeEnum(payment.getMethod()) : "N/A";
            binding.tvPaymentMethod.setText(String.format(java.util.Locale.US, "Method: %s", method));
            binding.tvPaymentAmount.setText(Formatters.formatCurrency(payment.getAmount()));

            if (payment.getBookingId() != null) {
                binding.tvPaymentLinked.setText(context.getString(R.string.payment_linked_slot_booking, payment.getBookingId()));
            } else if (payment.getGuideBookingId() != null) {
                binding.tvPaymentLinked.setText(context.getString(R.string.payment_linked_guide_booking, payment.getGuideBookingId()));
            } else {
                binding.tvPaymentLinked.setText(R.string.payment_unlinked_transaction);
            }

            String date = Formatters.formatDateTime(payment.getTransactionDate());
            binding.tvPaymentDate.setText(date.isEmpty() ? "" : String.format(java.util.Locale.US, "Date: %s", date));
        }
    }
}
