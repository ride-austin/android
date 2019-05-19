package com.rideaustin.ui.drawer.triphistory;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.databinding.FragmentTripDetailsBinding;
import com.rideaustin.ui.drawer.triphistory.base.BaseSupportTopicsFragment;

/**
 * Created by Sergey Petrov on 15/03/2017.
 */

public class TripDetailsFragment extends BaseSupportTopicsFragment implements TripDetailsViewModel.View {

    private TripDetailsViewModel viewModel;
    private FragmentTripDetailsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setToolbarTitleAligned(R.string.trip_history_details_title, Gravity.LEFT);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_trip_details, container, false);
        viewModel = new TripDetailsViewModel(this, binding);
        binding.setViewModel(viewModel);
        setViewModel(viewModel);
        setScrollView(binding.scrollView);
        setTopicsView(binding.viewSupportTopics);
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
    public boolean onBackPressed() {
        if (viewModel != null) {
            // RA-12894: onCreateView was not called ?!
            viewModel.onBackPressed();
        }
        return super.onBackPressed();
    }

    @Override
    public void onUnexpectedState() {
        // TODO: Decide what to do here
        // If data lost on app restart, we would go to login recently
        // If no data because of illegal state, better to throw an Exception
    }

}
