package com.rideaustin.ui.earnings;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.db.chart.view.BarChartView;
import com.rideaustin.R;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.databinding.ActivityWeeklyEarningsBinding;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.utils.CommonConstants;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by vokol on 10.08.2016.
 */
public class WeeklyEarningsActivity extends EngineStatelessActivity implements WeeklyEarningsViewModel.WeekEarningsView {
    private static final float NEXT_WEEK_INDICATOR_ANGLE = 180f;
    private static final String WEEK_ID = "week_id";

    private ActivityWeeklyEarningsBinding binding;
    private WeeklyEarningsViewModel viewModel;

    private LinearLayout calendarContainer;
    private boolean firstRun = true;
    private Subscription firstRunSubsciption = Subscriptions.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_weekly_earnings);
        binding.ivNextWeek.setRotation(NEXT_WEEK_INDICATOR_ANGLE);

        calendarContainer = binding.calendar;
        Toolbar toolbar = binding.toolbar;

        viewModel = new WeeklyEarningsViewModel(this, savedInstanceState != null ? savedInstanceState.getInt(WEEK_ID, 0) : 0);
        binding.setViewModel(viewModel);

        setSupportActionBar(toolbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.tvWeek.setOnClickListener(v -> viewModel.setNeedShowCalendarView(calendarContainer.getVisibility() != View.VISIBLE));
        binding.ivPreviousWeek.setOnClickListener(v -> viewModel.showPreviousWeek());
        binding.ivNextWeek.setOnClickListener(v -> viewModel.showNextWeek());
        binding.lvEarnings.setOnItemClickListener(onEarningSelectedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstRun) {
            // delay the job a bit not to affect slide animation
            firstRunSubsciption.unsubscribe();
            firstRunSubsciption = RxSchedulers.schedule(() -> {
                viewModel.init();
                firstRun = false;
            }, CommonConstants.ANIMATION_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // RA-9633: unsubscribe to prevent illegal fragment transactions
        firstRunSubsciption.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.onDestroy();
    }

    private final AdapterView.OnItemClickListener onEarningSelectedListener = (parent, view, position, id) -> {
        final Earning earning = (Earning) parent.getItemAtPosition(position);
        viewModel.selectEarning(earning, position);
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }

        return true;
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
    public BarChartView getBarChartView() {
        return binding.barchart;
    }

    @Override
    public ListView getEarningsListView() {
        return binding.lvEarnings;
    }

    @Override
    public void showCalendar(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(binding.calendar.getId(), fragment);
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewModel != null) {
            outState.putInt(WEEK_ID, viewModel.getWeekId());
        }
    }
}
