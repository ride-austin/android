package com.rideaustin.ui.drawer.cars.add;

import android.databinding.Bindable;

import com.rideaustin.App;
import com.rideaustin.BR;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.RxBaseObservable;

import java.util.List;

/**
 * Created by crossover on 24/01/2017.
 */

public class DriverVehicleInformationViewModel extends RxBaseObservable {

    private static final String DRIVER_REGISTRATION_PARAM = "driverRegistration";

    AddCarViewModel addCarViewModel;

    DriverVehicleInformationListener listener;

    private String vehicleDescription;

    public DriverVehicleInformationViewModel(AddCarViewModel addCarViewModel, DriverVehicleInformationListener listener) {
        this.listener = listener;
        this.addCarViewModel = addCarViewModel;
    }

    @Bindable
    public String getVehicleDescription() {
        return vehicleDescription;
    }

    interface DriverVehicleInformationListener {
        void onVehicleRequirementsChanged(final List<String> requirements);

        BaseActivityCallback getCallback();

        void onVehicleRequirementsFailed();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (addCarViewModel.getDriverRegistration() == null) {
            Integer cityId = App.getDataManager().getCurrentDriver().getCityId();
            listener.getCallback().showProgress();
            addSubscription(App.getDataManager()
                    .getConfigService()
                    .getDriverRegistration(cityId, DRIVER_REGISTRATION_PARAM)
                    .subscribeOn(RxSchedulers.network())
                    .observeOn(RxSchedulers.main())
                    .subscribe(driverRegistrationWrapper -> {
                        listener.getCallback().hideProgress();
                        addCarViewModel.setDriverRegistration(driverRegistrationWrapper.getDriverRegistration());
                        driverRegistrationLoaded(addCarViewModel.getDriverRegistration());
                    }, throwable -> {
                        listener.getCallback().hideProgress();
                        listener.onVehicleRequirementsFailed();
                    }));
        } else {
            driverRegistrationLoaded(addCarViewModel.getDriverRegistration());
        }
    }

    private void driverRegistrationLoaded(DriverRegistration driverRegistration) {
        listener.onVehicleRequirementsChanged(driverRegistration.getRequirements());
        vehicleDescription = driverRegistration.getDescription();
        notifyPropertyChanged(BR.vehicleDescription);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
