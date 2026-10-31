package com.example.horizontravel.ui.shared;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import com.example.horizontravel.R;
import com.example.horizontravel.databinding.ActivityContactUsBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Validators;

public class ContactUsActivity extends BaseActivity {

    private ActivityContactUsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupContactActions();
        setupForm();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupContactActions() {
        binding.rowCallPrimary.setOnClickListener(v -> {
            if (isSingleClick()) {
                launchDialer("+8801700123456");
            }
        });

        binding.rowCallEmergency.setOnClickListener(v -> {
            if (isSingleClick()) {
                launchDialer("+880999872835");
            }
        });

        binding.rowEmailSupport.setOnClickListener(v -> {
            if (isSingleClick()) {
                launchEmail("support@horizontravel.com", "Horizon Travel Inquiry");
            }
        });

        binding.rowEmailPartners.setOnClickListener(v -> {
            if (isSingleClick()) {
                launchEmail("partners@horizontravel.com", "Partner / Guide Inquiry");
            }
        });

        binding.rowMapAddress.setOnClickListener(v -> {
            if (isSingleClick()) {
                launchMap("Concord Tower, Gulshan 2, Dhaka, Bangladesh");
            }
        });
    }

    private void launchDialer(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showToast("No dialer application found");
        }
    }

    private void launchEmail(String email, String subject) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showToast("No email application found");
        }
    }

    private void launchMap(String query) {
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            startActivity(mapIntent);
        } catch (ActivityNotFoundException e) {
            showToast("No map application found");
        }
    }

    private void setupForm() {
        binding.btnSendContactMessage.setOnClickListener(v -> {
            if (!isSingleClick()) return;

            String name = binding.etContactName.getText() != null ? binding.etContactName.getText().toString().trim() : "";
            String email = binding.etContactEmail.getText() != null ? binding.etContactEmail.getText().toString().trim() : "";
            String phone = binding.etContactPhone.getText() != null ? binding.etContactPhone.getText().toString().trim() : "";
            String subject = binding.etContactSubject.getText() != null ? binding.etContactSubject.getText().toString().trim() : "";
            String message = binding.etContactMessage.getText() != null ? binding.etContactMessage.getText().toString().trim() : "";

            binding.tilContactName.setError(null);
            binding.tilContactEmail.setError(null);
            binding.tilContactSubject.setError(null);
            binding.tilContactMessage.setError(null);

            if (!Validators.isNotEmpty(name)) {
                binding.tilContactName.setError(getString(R.string.contact_err_name_required));
                binding.etContactName.requestFocus();
                return;
            }

            if (!Validators.isValidEmail(email)) {
                binding.tilContactEmail.setError(getString(R.string.contact_err_email_invalid));
                binding.etContactEmail.requestFocus();
                return;
            }

            if (!Validators.isNotEmpty(subject)) {
                binding.tilContactSubject.setError(getString(R.string.contact_err_subject_required));
                binding.etContactSubject.requestFocus();
                return;
            }

            if (!Validators.isNotEmpty(message)) {
                binding.tilContactMessage.setError(getString(R.string.contact_err_message_required));
                binding.etContactMessage.requestFocus();
                return;
            }

            // Successfully validated demo submission
            Snackbar.make(binding.getRoot(), getString(R.string.contact_success_message, name), Snackbar.LENGTH_LONG).show();

            // Clear inputs
            binding.etContactName.setText("");
            binding.etContactEmail.setText("");
            binding.etContactPhone.setText("");
            binding.etContactSubject.setText("");
            binding.etContactMessage.setText("");
            binding.etContactName.clearFocus();
            binding.etContactEmail.clearFocus();
            binding.etContactSubject.clearFocus();
            binding.etContactMessage.clearFocus();
        });
    }
}
