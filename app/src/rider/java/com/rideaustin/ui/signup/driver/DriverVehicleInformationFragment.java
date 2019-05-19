package com.rideaustin.ui.signup.driver;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rideaustin.R;
import com.rideaustin.databinding.DriverVehicleInformationBinding;

import java.util.List;

/**
 * Created by rost on 8/9/16.
 */
public class DriverVehicleInformationFragment extends BaseDriverSignUpFragment
        implements DriverVehicleInformationViewModel.DriverVehicleInformationListener {
    private DriverVehicleInformationBinding binding;
    private DriverVehicleInformationViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_vehicle_information, container, false);
        viewModel = new DriverVehicleInformationViewModel(getSignUpInteractor(), this);
        binding.setViewModel(viewModel);
        setToolbarTitle(R.string.title_driver_vehicle_information);
        setHasHelpWidget(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.continueButton.setOnClickListener(v -> notifyCompleted());
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

    private void showVehicleRequirements(List<String> vehicleRequirements) {
        binding.vehicleRequirementsContainer.removeAllViews();
        for (String vehicleRequirement : vehicleRequirements) {
            final TextView requirementView = (TextView) getLayoutInflater(null).inflate(R.layout.vehicle_requirement, null);
            requirementView.setText(vehicleRequirement);
            binding.vehicleRequirementsContainer.addView(requirementView);
        }
    }

    @Override
    public void onVehicleRequirementsChanged(final List<String> requirements) {
        showVehicleRequirements(requirements);
    }
}
