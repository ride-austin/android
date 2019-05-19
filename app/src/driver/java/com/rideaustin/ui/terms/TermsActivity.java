package com.rideaustin.ui.terms;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.R;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.TermsContainerBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;

/**
 * Created by Sergey Petrov on 24/05/2017.
 */

public class TermsActivity extends EngineStatelessActivity {

    private TermsContainerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        if (findFragmentById(R.id.content_frame) == null) {
            replaceFragment(new TermsFragment(), R.id.content_frame, false, Transition.NONE);
        }
    }
}
