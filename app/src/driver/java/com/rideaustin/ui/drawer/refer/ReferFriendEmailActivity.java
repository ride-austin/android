package com.rideaustin.ui.drawer.refer;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.rideaustin.R;
import com.rideaustin.databinding.ActivityReferFriendEmailBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.ui.utils.MaterialDialogCreator;

public class ReferFriendEmailActivity extends EngineStatelessActivity implements ReferFriendEmailViewModel.ViewModelListener{

    public static final String EMPTY = "";
    private ActivityReferFriendEmailBinding binding;
    private ReferFriendEmailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refer_friend_email);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.title.setText(R.string.refer_by_email_title);
        viewModel = new ReferFriendEmailViewModel(this);
        binding.setViewModel(viewModel);

        binding.sendButton.setOnClickListener(v -> {
            binding.email.setError(null);
            viewModel.validateAndSend();
        });
    }

    @Override
    public void onInputError() {
        binding.email.setError(getString(R.string.enter_valid_email));
    }

    @Override
    public void onCompleted() {
        hideProgress();
        MaterialDialogCreator.createInfoDialogCentered(getString(R.string.refer_friend_title), getString(R.string.refer_friend_email_success_msg), this).show();
        binding.email.setText(EMPTY);
    }

    @Override
    public void onError() {
        hideProgress();
        MaterialDialogCreator.createSimpleErrorDialog(getString(R.string.operation_failed), this).show();
    }

    @Override
    public void onStarted() {
        showProgress();
    }
}
