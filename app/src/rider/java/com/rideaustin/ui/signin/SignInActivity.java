package com.rideaustin.ui.signin;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.rideaustin.R;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.SignInContainerBinding;
import com.rideaustin.base.BaseActivity;

/**
 * Created by kshumelchyk on 7/9/16.
 */
public class SignInActivity extends BaseActivity {
    private SignInContainerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        replaceFragment(new SignInFragment(), R.id.content_frame, false, Transition.NONE);
    }

    @Override
    public boolean shouldBeLoggedIn() {
        return false;
    }
}
