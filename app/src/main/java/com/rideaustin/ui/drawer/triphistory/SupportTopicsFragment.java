package com.rideaustin.ui.drawer.triphistory;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.databinding.FragmentTripSupportTopicsBinding;
import com.rideaustin.ui.drawer.triphistory.base.BaseSupportTopicsFragment;

/**
 * Created by Sergey Petrov on 16/03/2017.
 */

public class SupportTopicsFragment extends BaseSupportTopicsFragment implements SupportTopicsViewModel.View {

    private static final String RIDE_KEY = "ride_key";
    private static final String PARENT_TOPIC_KEY = "parent_topic_key";

    private SupportTopicsViewModel viewModel;

    public static SupportTopicsFragment newInstance(long rideId, int parentTopicId) {
        SupportTopicsFragment fragment = new SupportTopicsFragment();
        Bundle args = new Bundle();
        args.putLong(RIDE_KEY, rideId);
        args.putInt(PARENT_TOPIC_KEY, parentTopicId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTripSupportTopicsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trip_support_topics, container, false);
        long rideId = getArguments() != null && getArguments().containsKey(RIDE_KEY) ? getArguments().getLong(RIDE_KEY) : -1;
        int parentTopicId = getArguments() != null && getArguments().containsKey(PARENT_TOPIC_KEY) ? getArguments().getInt(PARENT_TOPIC_KEY) : -1;
        viewModel = new SupportTopicsViewModel(this, rideId, parentTopicId);
        binding.setViewModel(viewModel);
        setViewModel(viewModel);
        setScrollView(binding.scrollView);
        setTopicsView(binding.viewSupportTopics);

        String title = viewModel.getTitle();
        if (title != null) {
            setToolbarTitleAligned(title, Gravity.LEFT);
        } else {
            setToolbarTitleAligned(R.string.trip_history_support_topics_title, Gravity.LEFT);
        }
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    @Override
    public void onUnexpectedState() {
        // TODO: Decide what to do here
        // If data lost on app restart, we would go to login recently
        // If no data because of illegal state, better to throw an Exception
    }
}
