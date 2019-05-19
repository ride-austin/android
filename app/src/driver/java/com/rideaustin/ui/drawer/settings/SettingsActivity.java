package com.rideaustin.ui.drawer.settings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.SettingsContainerBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;


/**
 * Created by ysych on 8/15/16.
 */
public class SettingsActivity extends EngineStatelessActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsContainerBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        if (findFragmentById(R.id.content_frame) == null) {
            replaceFragment(new SettingsFragment(), R.id.content_frame, false, Transition.NONE);
        }
    }
}
