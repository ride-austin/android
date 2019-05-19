package com.rideaustin.ui.stats;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.rideaustin.R;
import com.rideaustin.databinding.ActivityStatsBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.ui.common.SafeLinearLayoutManager;
import com.rideaustin.ui.genericsupport.GenericContactSupportActivity;

/**
 * Created by Sergey Petrov on 26/07/2017.
 */

public class StatsActivity extends EngineStatelessActivity {

    private StatsAdapter adapter;
    private StatsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStatsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_stats);
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.title.setText(R.string.stats_title);

        RecyclerView recyclerView = binding.rvStats;
        recyclerView.setLayoutManager(new SafeLinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(adapter = new StatsAdapter());
        binding.setViewModel(viewModel = new StatsViewModel(adapter, this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.button_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.item);
        Button button = menuItem.getActionView().findViewById(R.id.button);
        button.setText(R.string.menu_contact);
        button.setOnClickListener(v -> onOptionsItemSelected(menuItem));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item) {
            startActivity(new Intent(this, GenericContactSupportActivity.class));
            return true;
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
}
