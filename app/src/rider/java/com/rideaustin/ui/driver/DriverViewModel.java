package com.rideaustin.ui.driver;

import android.databinding.BaseObservable;
import android.databinding.BindingAdapter;
import android.databinding.ObservableField;
import android.widget.ImageView;

import com.rideaustin.App;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.base.ApiSubscriber;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.signup.driver.DriverSignUpInteractor;
import com.rideaustin.ui.utils.ResourceHelper;
import com.rideaustin.utils.ImageHelper;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by hatak on 17.11.16.
 */

public class DriverViewModel extends BaseObservable {

    private static final String DRIVER_REGISTRATION_PARAM = "driverRegistration";

    private final DriverViewModelListener listener;
    private ObservableField<CityModel> selectedCity = new ObservableField<>();
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private DriverSignUpInteractor signUpInteractor = new DriverSignUpInteractor();

    public DriverViewModel(final DriverViewModelListener listener) {
        this.listener = listener;
    }

    public void onStart() {
        listener.onConfigChanged(App.getConfigurationManager().getLastConfiguration());
    }

    public void onStop() {
        subscriptions.clear();
    }


    public ObservableField<CityModel> getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(CityModel selectedCity) {
        this.selectedCity.set(selectedCity);
    }

    @BindingAdapter("driverCityLogo")
    public static void loadLogo(ImageView view, String imageUrl) {
        Timber.d("Loading city logo by url: %s", imageUrl);
        int placeholder = ResourceHelper.getBlackLogoDrawableRes(App.getConfigurationManager().getLastConfiguration());
        ImageHelper.loadImageIntoView(view, imageUrl, placeholder);
    }

    public void loadDriverRegistrationConfiguration(final long selectedCityId) {
        subscriptions.add(App.getDataManager()
                .getConfigService()
                .getDriverRegistration(selectedCityId, DRIVER_REGISTRATION_PARAM)
                .zipWith(App.getDataManager()
                        .getConfigService()
                        .getGlobalConfigRider(selectedCityId), (driverRegistrationWrapper, globalConfig) ->
                        new Configurations(globalConfig, driverRegistrationWrapper.getDriverRegistration()))
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(new ApiSubscriber<Configurations>(listener.getCallback()) {
                    @Override
                    public void onNext(final Configurations configurations) {
                        super.onNext(configurations);
                        signUpInteractor.setSelectedCityId(selectedCityId);
                        signUpInteractor.setDriverRegistrationConfiguration(configurations.driverRegistration);
                        signUpInteractor.setGlobalConfigInRegistration(configurations.globalConfig);
                        listener.onRegistrationConfigurationLoaded();
                    }

                    @Override
                    public void onError(final BaseApiException e) {
                        super.onError(e);
                        listener.onRegistrationConfigurationFailed();
                    }
                }));
    }

    public DriverSignUpInteractor getSignUpInteractor() {
        return signUpInteractor;
    }

    private static class Configurations {
        private final GlobalConfig globalConfig;
        private final DriverRegistration driverRegistration;

        private Configurations(final GlobalConfig globalConfig, final DriverRegistration driverRegistration) {
            this.globalConfig = globalConfig;
            this.driverRegistration = driverRegistration;
        }
    }

    public interface DriverViewModelListener extends BaseActivityCallback {

        void onConfigChanged(GlobalConfig globalConfig);

        void onRegistrationConfigurationLoaded();

        void onRegistrationConfigurationFailed();

        BaseActivityCallback getCallback();
    }
}
