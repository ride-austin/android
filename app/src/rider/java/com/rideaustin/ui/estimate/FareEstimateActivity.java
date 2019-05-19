package com.rideaustin.ui.estimate;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MenuItem;

import com.rideaustin.R;
import com.rideaustin.databinding.ActivityFareEstimateBinding;
import com.rideaustin.ui.base.BaseRiderActivity;

/**
 * Created by vokol on 30.06.2016.
 */
public class FareEstimateActivity extends BaseRiderActivity {

    private ActivityFareEstimateBinding binding;
    private FareEstimateViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_fare_estimate);
        this.viewModel = new FareEstimateViewModel(getIntent(), this);
        this.binding.setViewModel(viewModel);

        setSupportActionBar(this.binding.toolbar);
        binding.toolbarTitle.setText(R.string.title_fare_estimate);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        viewModel.showFareEstimateAmount();
    }

    @Override
    public void onStart() {
        super.onStart();
        enterDestination();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    public void enterDestination() {
        binding.btnSetNewDestination.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
