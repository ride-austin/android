package com.rideaustin.ui.map;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rideaustin.R;
import com.rideaustin.base.BaseFragment;
import com.rideaustin.databinding.DriverInfoBinding;
import com.rideaustin.ui.utils.UIUtils;

/**
 * Created by ysych on 8/17/16.
 */
public class DriverInfoFragment extends BaseFragment {

    private String driverName;
    private String carMake;
    private String plateNumber;
    private double driverRating;
    private DriverInfoBinding binding;

    public static DriverInfoFragment newInstance(String driverName, String carMake, String plateNumber, double rating) {
        final DriverInfoFragment driverInfoFragment = new DriverInfoFragment();
        driverInfoFragment.setDriverInfo(driverName, carMake, plateNumber, rating);
        return driverInfoFragment;
    }

    private void setDriverInfo(String driverName, String carMake, String plateNumber, double rating) {
        this.driverName = driverName;
        this.carMake = carMake;
        this.plateNumber = plateNumber;
        this.driverRating = rating;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_driver_info, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDriverInfo();
    }

    private void setupDriverInfo() {
        if (binding != null) {
            binding.tvDriverName.setText(driverName);
            binding.tvCarMake.setText(carMake);
            binding.tvPlateNumber.setText(plateNumber);
            binding.tvDriverRate.setText(UIUtils.formatRating(driverRating));
        }
    }

}

