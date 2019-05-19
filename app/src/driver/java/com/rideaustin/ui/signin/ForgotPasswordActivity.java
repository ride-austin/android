package com.rideaustin.ui.signin;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.rideaustin.R;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.ForgotPassBinding;

/**
 * Created by kshumelchyk on 7/12/16.
 */
public class ForgotPasswordActivity extends BaseActivity {
    private ForgotPassBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        replaceFragment(new ResetPasswordFragment(), R.id.content_frame, false, Transition.NONE);
    }

    @Override
    public boolean shouldBeLoggedIn() {
        return false;
    }
}