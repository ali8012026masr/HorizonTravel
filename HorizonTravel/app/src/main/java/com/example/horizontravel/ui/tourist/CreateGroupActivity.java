package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Group;
import com.example.horizontravel.data.model.GroupCreateRequest;
import com.example.horizontravel.databinding.ActivityCreateGroupBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

public class CreateGroupActivity extends BaseActivity {

    private ActivityCreateGroupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSubmitCreateGroup.setOnClickListener(v -> submitCreateGroup());
    }

    private void submitCreateGroup() {
        String groupName = binding.etGroupName.getText() != null ? binding.etGroupName.getText().toString().trim() : "";
        if (groupName.isEmpty()) {
            binding.tilGroupName.setError(getString(R.string.error_field_required));
            return;
        }
        binding.tilGroupName.setError(null);

        long touristId = SessionManager.getInstance().getUserId();
        GroupCreateRequest request = new GroupCreateRequest(groupName, touristId);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitCreateGroup.setEnabled(false);

        ApiClient.getApiService().createGroup(request).enqueue(new ApiCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateGroupActivity.this, R.string.toast_group_created_successful, Toast.LENGTH_SHORT).show();

                if (group != null && group.getId() != null) {
                    Intent intent = new Intent(CreateGroupActivity.this, GroupDetailActivity.class);
                    intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, group.getId());
                    startActivity(intent);
                }
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmitCreateGroup.setEnabled(true);
                Toast.makeText(CreateGroupActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
