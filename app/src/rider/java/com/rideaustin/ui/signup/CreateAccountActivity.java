package com.rideaustin.ui.signup;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.CreateAccountBinding;

/**
 * Created by kshumelchyk on 6/23/16.
 */
public class CreateAccountActivity extends BaseActivity {

    public static String EXTRA_MESSAGE_KEY = "EXTRA_MESSAGE_KEY";

    private CreateAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_account);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        CreateAccountFragment createAccountFragment = new CreateAccountFragment();
        createAccountFragment.setArguments(getIntent().getExtras());
        replaceFragment(createAccountFragment, R.id.content_frame, false, Transition.NONE);
    }

    @Override
    public boolean shouldBeLoggedIn() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
