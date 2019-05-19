package com.rideaustin.ui.drawer.editprofile;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MenuItem;

import com.rideaustin.R;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.databinding.ActivityUpdatePasswordBinding;
import com.rideaustin.ui.utils.MaterialDialogCreator;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by rideclientandroid on 4.10.2016.
 */

public class UpdatePasswordActivity extends BaseActivity {
    private ActivityUpdatePasswordBinding binding;
    private UpdatePasswordViewModel viewModel;

    private CompositeSubscription subscription = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_password);
        viewModel = new UpdatePasswordViewModel();
        binding.setViewModel(viewModel);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.btnSumbitPassword.setOnClickListener(v -> viewModel.submitPassword(this));
    }

    @Override
    public void onStart() {
        subscription.add(viewModel.subscribeValidationErrors().subscribe(this::onValidationError));
        subscription.add(viewModel.subscribePasswordSubmitResults().subscribe(this::onPasswordSubmitted));
        super.onStart();
    }

    private void onPasswordSubmitted(Boolean isSuccess) {
        if (isSuccess) {
            finish();
        }
    }

    private void onValidationError(Integer errorMessage) {
        MaterialDialogCreator.createErrorDialog(getString(R.string.text_oops),
                getString(errorMessage), this);
    }

    @Override
    public void onStop() {
        subscription.clear();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
