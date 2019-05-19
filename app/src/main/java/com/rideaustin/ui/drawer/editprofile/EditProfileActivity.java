package com.rideaustin.ui.drawer.editprofile;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.base.BaseActivity;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.EditProfileBinding;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;

/**
 * Created by Sergey Petrov on 20/02/2017.
 */

public class EditProfileActivity extends BaseActivity {

    public static final String HAS_BACK_ACTION = "has_back_action";

    private EditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);
        if (!getIntent().hasExtra(HAS_BACK_ACTION)) {
            binding.toolbar.setNavigationIcon(null);
        } else {
            binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        replaceFragment(new EditProfileFragment(), R.id.content_frame, false, Transition.NONE);
    }

    public void close() {
        if (getIntent().hasExtra(HAS_BACK_ACTION)) {
            onBackPressed();
        } else {
            startActivity(new Intent(this, NavigationDrawerActivity.class));
            finish();
        }
    }
}
