package com.rideaustin.ui.signup;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.api.model.User;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.CharityBinding;
import com.rideaustin.ui.base.BaseRiderActivity;
import com.rideaustin.ui.drawer.NavigationDrawerActivity;
import com.rideaustin.ui.drawer.donate.DonateFragment;
import com.rideaustin.ui.drawer.editprofile.EditProfileActivity;
import com.rideaustin.ui.driver.DriverActivity;
import com.rideaustin.ui.utils.ProfileValidator;

/**
 * Created by kshumelchyk on 8/10/16.
 */
public class CharityActivity extends BaseRiderActivity {

    private CharityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_charity);

        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        binding.next.setOnClickListener(v -> onNextClicked());

        replaceFragment(new DonateFragment(), R.id.content_frame, false, Transition.NONE);
    }

    private void onNextClicked() {
        startActivity(new Intent(CharityActivity.this, DriverActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isUserDataValid()) {
            startActivity(new Intent(this, NavigationDrawerActivity.class));
        } else {
            startActivity(new Intent(this, EditProfileActivity.class));
        }
        finish();
    }

    private boolean isUserDataValid() {
        User user = App.getDataManager().getCurrentUser();
        return ProfileValidator.validateUser(user, this);
    }

}
