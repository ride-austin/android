package com.rideaustin.ui.drawer.cars.add;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.databinding.DriverVehicleInformationSummaryBinding;
import com.rideaustin.ui.utils.MenuUtil;

import rx.subscriptions.CompositeSubscription;

public class DriverVehicleInformationSummaryFragment extends BaseAddCarFragment {

    private DriverVehicleInformationSummaryBinding binding;
    private CompositeSubscription subscriptions = new CompositeSubscription();


    public static DriverVehicleInformationSummaryFragment newInstance(AddCarActivity.AddCarSequence sequence) {
        DriverVehicleInformationSummaryFragment fragment = new DriverVehicleInformationSummaryFragment();
        Bundle args = new Bundle();
        args.putSerializable(SEQUENCE_KEY, sequence);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_vehicle_information_summary, container, false);
        setToolbarTitle(R.string.title_driver_vehicle_information);

        binding.summary.setText(addCarViewModel.getDriverRegistration().getNewCarSuccessMessage());
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuUtil.inflateNextMenu(menu, inflater, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                notifyCompleted();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        subscriptions.clear();
    }
}
