package com.rideaustin.ui.earnings;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.rideaustin.App;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.databinding.ActivityDailyEarningsBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.utils.ViewUtils;


/**
 * Created by vokol on 16.08.2016.
 */
public class DailyEarningActivity extends EngineStatelessActivity implements DailyEarningsViewModel.DailyEarningsView {
    private ActivityDailyEarningsBinding binding;
    private DailyEarningsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, com.rideaustin.R.layout.activity_daily_earnings);
        setSupportActionBar(binding.toolbar);

        viewModel = new DailyEarningsViewModel(this);

        if (!App.getDataManager().isLoggedIn()) {
            // RA-9292: nothing to do here if app is not logged in
            // would go to splash onStart
            return;
        }
        viewModel.init(getIntent().getExtras());
        binding.setViewModel(viewModel);

        int width = getResources().getDisplayMetrics().widthPixels;
        int wOffset = (int) ViewUtils.dpToPixels(40);
        int hOffset = (int) ViewUtils.dpToPixels(10);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            binding.tripsHistory.setIndicatorBounds(width - wOffset, width - hOffset);
        } else {
            binding.tripsHistory.setIndicatorBoundsRelative(width - wOffset, width - hOffset);
        }
        binding.tripsHistory.setOnGroupExpandListener(viewModel::onListGroupExpanded);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            viewModel.onDestroy();
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public BaseActivityCallback getCallback() {
        return this;
    }

    @Override
    public ExpandableListView getTripHistoryList() {
        return binding.tripsHistory;
    }
}
