package com.rideaustin.ui.drawer.refer;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.rideaustin.R;
import com.rideaustin.databinding.ActivityReferFriendTextBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.ui.utils.MaterialDialogCreator;
import com.rideaustin.ui.utils.phone.PhoneInputUtil;
import com.rideaustin.utils.Constants;

public class ReferFriendTextActivity extends EngineStatelessActivity implements ReferFriendTextViewModel.ViewModelListener {

    private ActivityReferFriendTextBinding binding;
    private ReferFriendTextViewModel viewModel;
    private PhoneInputUtil phoneInputUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_refer_friend_text);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.title.setText(R.string.refer_by_phone_title);
        phoneInputUtil = new PhoneInputUtil(this, binding.country, binding.mobile);
        viewModel = new ReferFriendTextViewModel(this, phoneInputUtil);
        binding.setViewModel(viewModel);
        binding.sendButton.setOnClickListener(v -> viewModel.validateAndSend());
    }

    @Override
    public void onInputError() {
        binding.mobile.setError(getString(R.string.enter_valid_phone_msg));
    }

    @Override
    public void onCompleted() {
        hideProgress();
        MaterialDialogCreator.createInfoDialogCentered(getString(R.string.refer_friend_title), getString(R.string.refer_friend_sms_success_msg), this).show();
        binding.mobile.setText(Constants.EMPTY_STRING);
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
