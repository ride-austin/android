package com.rideaustin.ui.signup.driver;

import android.databinding.BaseObservable;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.rideaustin.App;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.models.DriverRegistrationData;
import com.rideaustin.models.VehicleManager;
import com.rideaustin.ui.utils.ResourceHelper;
import com.rideaustin.utils.ImageHelper;

/**
 * Created by hatak on 18.11.16.
 */

public class DriverSignUpViewModel extends BaseObservable {

    private final DriverSignUpActivityCallback listener;
    private final DriverSignUpInteractor signUpInteractor;
    private String logoUrl;
    private static Target<Bitmap> cityLogoTarget;

    public DriverSignUpViewModel(final DriverSignUpActivityCallback listener, final DriverSignUpInteractor signUpInteractor) {
        this.listener = listener;
        this.signUpInteractor = signUpInteractor;
    }

    public DriverSignUpInteractor getSignUpInteractor() {
        return signUpInteractor;
    }

    public DriverRegistrationData getDriverRegistrationData() {
        return signUpInteractor.getDriverRegistrationData();
    }

    public void setCityId(final int cityId) {
        signUpInteractor.setSelectedCityId(cityId);
    }

    public void loadLogo(ImageView view, String imageUrl) {
        int placeholder = ResourceHelper.getBlackLogoDrawableRes(App.getConfigurationManager().getLastConfiguration());
        cityLogoTarget = ImageHelper.loadImageIntoView(view, imageUrl, placeholder);
    }

    public void onStart() {
        GlobalConfig globalConfigInRegistration = signUpInteractor.getGlobalConfigInRegistration();
        listener.onConfigUpdated(globalConfigInRegistration);
    }

    public void onStop() {
        if (cityLogoTarget != null) {
            Glide.with(App.getInstance()).clear(cityLogoTarget);
        }
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    interface DriverSignUpActivityCallback extends BaseActivityCallback {

        DriverSignUpViewModel getViewModelInternal();

        VehicleManager getVehicleManager();

        void onCompleted();

        void onRequestHelpWidget(boolean hasWidget);

        void onConfigUpdated(final GlobalConfig globalConfig);
    }


}
