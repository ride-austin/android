package com.rideaustin.ui.drawer.queue;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.rideaustin.App;
import com.rideaustin.R;
import com.rideaustin.base.Transition;
import com.rideaustin.databinding.QueueContainerBinding;
import com.rideaustin.ui.base.EngineStatelessActivity;
import com.rideaustin.utils.Constants;

/**
 * Created on 9/20/16.
 */
public class QueueActivity extends EngineStatelessActivity {

    private QueueContainerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!App.getDataManager().isLoggedIn()) {
            super.onCreate(null);
            return;
        }

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_airport_queue);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        binding.queueName.setText(getString(R.string.dynamic_queue_name, getQueueName()));
        if (findFragmentById(R.id.content_airport) == null) {
            replaceFragment(new QueueFragment(), R.id.content_airport, false, Transition.NONE);
        }
    }

    public String getQueueName() {
        return getIntent().getStringExtra(Constants.QUEUE_NAME);
    }
}
