package com.rideaustin.ui.signup.driver;

import android.databinding.Bindable;

import com.rideaustin.BR;
import com.rideaustin.ui.common.RxBaseObservable;

import java.util.List;

/**
 * Created by hatak on 18.11.16.
 */

public class DriverVehicleInformationViewModel extends RxBaseObservable {

    private final DriverVehicleInformationListener listener;
    private final DriverSignUpInteractor signUpInteractor;

    private String vehicleDescription;

    public DriverVehicleInformationViewModel(final DriverSignUpInteractor signUpInteractor, final DriverVehicleInformationListener listener) {
        this.listener = listener;
        this.signUpInteractor = signUpInteractor;
    }

    @Override
    public void onStart() {
        super.onStart();
        vehicleDescription = signUpInteractor.getDriverRegistrationConfiguration().getDescription();
        listener.onVehicleRequirementsChanged(signUpInteractor.getDriverRegistrationConfiguration().getRequirements());
        notifyPropertyChanged(BR.vehicleDescription);
    }

    @Bindable
    public String getVehicleDescription() {
        return vehicleDescription;
    }

    interface DriverVehicleInformationListener {
        void onVehicleRequirementsChanged(final List<String> requirements);
    }

}
