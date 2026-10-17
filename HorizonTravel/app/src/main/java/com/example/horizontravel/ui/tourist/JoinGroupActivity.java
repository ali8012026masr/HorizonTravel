package com.example.horizontravel.ui.tourist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.horizontravel.R;
import com.example.horizontravel.data.api.ApiCallback;
import com.example.horizontravel.data.api.ApiClient;
import com.example.horizontravel.data.model.Group;
import com.example.horizontravel.data.model.JoinGroupRequest;
import com.example.horizontravel.databinding.ActivityJoinGroupBinding;
import com.example.horizontravel.ui.BaseActivity;
import com.example.horizontravel.util.SessionManager;

public class JoinGroupActivity extends BaseActivity {

    private ActivityJoinGroupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJoinGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSubmitJoin.setOnClickListener(v -> submitJoin());
    }

    private void submitJoin() {
        String code = binding.etJoinCode.getText() != null ? binding.etJoinCode.getText().toString().trim() : "";
        if (code.isEmpty()) {
            binding.tilJoinCode.setError(getString(R.string.error_field_required));
            return;
        }
        binding.tilJoinCode.setError(null);

        long touristId = SessionManager.getInstance().getUserId();
        JoinGroupRequest request = new JoinGroupRequest(code, touristId);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitJoin.setEnabled(false);

        ApiClient.getApiService().joinGroup(request).enqueue(new ApiCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(JoinGroupActivity.this, R.string.group_join_success, Toast.LENGTH_SHORT).show();

                if (group != null && group.getId() != null) {
                    Intent intent = new Intent(JoinGroupActivity.this, GroupDetailActivity.class);
                    intent.putExtra(GroupDetailActivity.EXTRA_GROUP_ID, group.getId());
                    startActivity(intent);
                }
                finish();
            }

            @Override
            public void onError(String message, int statusCode) {
                if (isFinishing() || isDestroyed()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmitJoin.setEnabled(true);
                Toast.makeText(JoinGroupActivity.this,
                        message != null ? message : getString(R.string.error_generic),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
