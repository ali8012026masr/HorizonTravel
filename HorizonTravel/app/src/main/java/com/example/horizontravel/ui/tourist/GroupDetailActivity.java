package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Group;
import com.example.horizontravel.data.model.GroupBookingRequest;
import com.example.horizontravel.data.model.GroupMember;
import com.example.horizontravel.databinding.ActivityGroupDetailBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.Formatters;
import com.example.horizontravel.util.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class GroupDetailActivity extends BaseActivity {

    public static final String EXTRA_GROUP_ID = "extra_group_id";

    private ActivityGroupDetailBinding binding;
    private long groupId;
    private Group currentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1);
        if (groupId <= 0) {
            Toast.makeText(this, R.string.toast_invalid_group, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupListeners();
        loadGroup();
    }

    private void setupListeners() {
        binding.btnOpenChat.setOnClickListener(v -> {
            if (currentGroup != null) {
                Intent intent = new Intent(this, GroupChatActivity.class);
                intent.putExtra(GroupChatActivity.EXTRA_GROUP_ID, currentGroup.getId());
                intent.putExtra(GroupChatActivity.EXTRA_GROUP_NAME, currentGroup.getGroupName());
                startActivity(intent);
            }
        });

        binding.btnLinkBooking.setOnClickListener(v -> showLinkBookingDialog());

        binding.btnLeaveGroup.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.btn_leave_group)
                    .setMessage(R.string.msg_are_you_sure_you_want_to_l)
                    .setPositiveButton(R.string.yes, (dialog, which) -> leaveGroup())
                    .setNegativeButton(R.string.no, null)
                    .show();
        });
    }

    private void loadGroup() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getGroupById(groupId).enqueue(new ApiCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                currentGroup = group;
                displayGroup(group);
                loadMembers();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupDetailActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayGroup(Group group) {
        if (group == null) return;

        binding.tvGroupName.setText(group.getGroupName() != null ? group.getGroupName() : "Travel Group");

        String code = group.getJoinCode() != null ? group.getJoinCode() : "";
        binding.tvJoinCode.setText(getString(R.string.group_code_label, code));

        if (group.getBookingId() != null && group.getBookingId() > 0) {
            binding.tvLinkedBooking.setText(String.format(java.util.Locale.US, "Linked Booking: #%d", group.getBookingId()));
            binding.btnLinkBooking.setVisibility(View.GONE);
        } else {
            binding.tvLinkedBooking.setText(R.string.text_linked_booking__none);
            binding.btnLinkBooking.setVisibility(View.VISIBLE);
        }
    }

    private void loadMembers() {
        ApiClient.getApiService().getGroupMembers(groupId).enqueue(new ApiCallback<List<GroupMember>>() {
            @Override
            public void onSuccess(List<GroupMember> members) {
                if (isFinishing() || isDestroyed()) return;
                binding.layoutMembersContainer.removeAllViews();

                if (members != null) {
                    binding.tvMembersHeader.setText(getString(R.string.group_members_label, members.size()));
                    long myId = SessionManager.getInstance().getUserId();
                    LayoutInflater inflater = LayoutInflater.from(GroupDetailActivity.this);

                    for (GroupMember m : members) {
                        com.example.horizontravel.databinding.ItemTwoLineBinding itemBinding =
                                com.example.horizontravel.databinding.ItemTwoLineBinding.inflate(inflater, binding.layoutMembersContainer, false);

                        String name = m.getTouristName() != null ? m.getTouristName() : "Member #" + m.getTouristId();
                        boolean isMe = m.getTouristId() != null && m.getTouristId() == myId;
                        if (isMe) {
                            name += " (You)";
                        }
                        itemBinding.tvText1.setText(name);
                        itemBinding.tvText1.setTextColor(getResources().getColor(R.color.navy_primary));

                        boolean paid = Boolean.TRUE.equals(m.getPaid());
                        String shareText = String.format(java.util.Locale.US, "Share: %s  |  Status: %s",
                                Formatters.formatCurrency(m.getAmountOwed()),
                                paid ? "Paid" : "Unpaid");
                        itemBinding.tvText2.setText(shareText);
                        itemBinding.tvText2.setTextColor(getResources().getColor(R.color.on_surface_variant));

                        // If it's me and unpaid, show quick pay share on click
                        if (isMe && !paid) {
                            itemBinding.getRoot().setOnClickListener(v -> showPayShareDialog(m));
                        }

                        binding.layoutMembersContainer.addView(itemBinding.getRoot());
                    }
                }
            }

            @Override
            public void onError(String message, int statusCode) {}
        });
    }

    private void showPayShareDialog(GroupMember member) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_pay_share)
                .setMessage(String.format(java.util.Locale.US, "Mark your share of %s as paid?", Formatters.formatCurrency(member.getAmountOwed())))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    markSharePaid();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void markSharePaid() {
        long myId = SessionManager.getInstance().getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().markGroupMemberPaid(groupId, myId).enqueue(new ApiCallback<GroupMember>() {
            @Override
            public void onSuccess(GroupMember member) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupDetailActivity.this, R.string.toast_share_marked_as_paid, Toast.LENGTH_SHORT).show();
                loadMembers();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupDetailActivity.this,
                        message != null ? message : "Unable to mark paid",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLinkBookingDialog() {
        final EditText etBookingId = new EditText(this);
        etBookingId.setHint("Booking ID (e.g. 101)");
        etBookingId.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle(R.string.link_booking_title)
                .setView(etBookingId)
                .setPositiveButton(R.string.link_booking_btn, (dialog, which) -> {
                    String text = etBookingId.getText().toString().trim();
                    if (!text.isEmpty()) {
                        try {
                            long bookingId = Long.parseLong(text);
                            linkBooking(bookingId);
                        } catch (NumberFormatException ignored) {}
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void linkBooking(long bookingId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        GroupBookingRequest request = new GroupBookingRequest(bookingId);

        ApiClient.getApiService().linkGroupBooking(groupId, request).enqueue(new ApiCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupDetailActivity.this, R.string.link_booking_success, Toast.LENGTH_SHORT).show();
                currentGroup = group;
                displayGroup(group);
                loadMembers();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupDetailActivity.this,
                        message != null ? message : "Unable to link booking",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveGroup() {
        long myId = SessionManager.getInstance().getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().leaveOrRemoveGroupMember(groupId, myId).enqueue(new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupDetailActivity.this, R.string.toast_you_left_the_group, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(GroupDetailActivity.this,
                        message != null ? message : "Unable to leave group",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
