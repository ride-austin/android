package com.rideaustin.ui.drawer.promotions;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.StringRes;

import com.rideaustin.R;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.PromotionsActivityBinding;
import com.rideaustin.ui.base.BaseRiderActivity;

/**
 * Created by kshumelchyk on 10/4/16.
 */
public class PromotionsActivity extends BaseRiderActivity {

    private PromotionsActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promotions);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        replaceFragment(new PromotionsFragment(), R.id.content_frame, false, Transition.NONE);
    }

    public void setTitle(@StringRes int title) {
        binding.toolbarTitle.setText(title);
    }
}
