package com.rideaustin.ui.drawer.riderequest;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.rideaustin.R;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.RideRequestTypeContainerBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;


/**
 * Created by RideDriver on 8/18/16.
 */
public class RideRequestTypeActivity extends EngineStatelessActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RideRequestTypeContainerBinding binding = DataBindingUtil.setContentView(RideRequestTypeActivity.this, R.layout
                .activity_ride_request_type);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        if (findFragmentById(R.id.content_frame) == null) {
            replaceFragment(RideRequestTypeFragment.newInstance(), R.id.content_frame, false, Transition.NONE);
        }
    }
}
