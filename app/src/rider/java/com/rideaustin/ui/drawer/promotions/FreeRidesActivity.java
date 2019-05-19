package com.rideaustin.ui.drawer.promotions;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.FreeRidesActivityBinding;
import com.rideaustin.ui.base.BaseRiderActivity;

/**
 * Created by kshumelchyk on 10/5/16.
 */
public class FreeRidesActivity extends BaseRiderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FreeRidesActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_free_rides);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        replaceFragment(new FreeRidesFragment(), R.id.content_frame, false, Transition.NONE);
    }
}
