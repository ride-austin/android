package com.rideaustin.ui.signup;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.FacebookLoginBinding;
import com.rideaustin.base.BaseActivity;

/**
 * Created by kshumelchyk on 6/24/16.
 */
public class FacebookLoginActivity extends BaseActivity {

    private FacebookLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            App.getDataManager().restoreUserRegistrationData(savedInstanceState);
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_facebook_login);

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        replaceFragment(new EnterMobileFragment(), R.id.content_frame, false, Transition.NONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        App.getDataManager().saveUserRegistrationData(outState);
    }

    @Override
    public boolean shouldBeLoggedIn() {
        return false;
    }
}
