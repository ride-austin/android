package com.rideaustin.ui.signup.driver;

import android.app.Activity;

import com.rideaustin.base.BaseFragment;
import com.rideaustin.models.DriverRegistrationData;
import com.rideaustin.models.VehicleManager;

/**
 * Created by rost on 8/10/16.
 */
public class BaseDriverSignUpFragment extends BaseFragment {

    private DriverSignUpViewModel.DriverSignUpActivityCallback baseSignUpActivityCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        baseSignUpActivityCallback = (DriverSignUpViewModel.DriverSignUpActivityCallback) activity;
    }

    protected void notifyCompleted() {
        baseSignUpActivityCallback.onCompleted();
    }

    protected DriverRegistrationData getDriverData() {
        return baseSignUpActivityCallback.getViewModelInternal().getDriverRegistrationData();
    }

    protected VehicleManager getVehicleManager() {
        return baseSignUpActivityCallback.getVehicleManager();
    }

    protected DriverSignUpInteractor getSignUpInteractor() {
        return baseSignUpActivityCallback.getViewModelInternal().getSignUpInteractor();
    }

    protected void setHasHelpWidget(boolean hasWidget) {
        baseSignUpActivityCallback.onRequestHelpWidget(hasWidget);
    }

    protected void clearState() {

    }
}
