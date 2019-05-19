package com.rideaustin.ui.drawer.refer;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.databinding.ActivityReferFriendBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;

public class ReferFriendActivity extends EngineStatelessActivity {

    private ActivityReferFriendBinding binding;
    private ReferFriendViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refer_friend);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.title.setText(R.string.refer_friend_title);
        binding.toolbarTitle.setText(App.getFormattedAppName());
        viewModel = new ReferFriendViewModel();

        binding.emailButton.setOnClickListener(v -> referByEmailClicked());
        binding.textButton.setOnClickListener(v -> referBySmsClicked());

        binding.setViewModel(viewModel);
    }

    private void referByEmailClicked() {
        startActivity(new Intent(ReferFriendActivity.this, ReferFriendEmailActivity.class));
    }

    private void referBySmsClicked() {
        startActivity(new Intent(ReferFriendActivity.this, ReferFriendTextActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }
}
