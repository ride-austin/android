package com.rideaustin.ui.signup.driver.fcra_disclosure;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.rideaustin.App;
import com.rideaustin.api.config.SupportedCity;
import com.rideaustin.api.model.driver.DriverRegistration;
import com.rideaustin.manager.ConfigurationManager;
import com.rideaustin.models.DriverRegistrationData;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.ui.common.DefaultErrorAction;
import com.rideaustin.utils.AssetsHelper;

import rx.Observable;

/**
 * Created by Viktor Kifer
 * On 03-Jan-2017.
 */

public class FcraDisclosureViewModel extends BaseViewModel<FcraDisclosureView> {

    private ConfigurationManager configurationManager;

    public FcraDisclosureViewModel(@NonNull FcraDisclosureView view) {
        this(view, App.getConfigurationManager());
    }

    @VisibleForTesting
    FcraDisclosureViewModel(@NonNull FcraDisclosureView view,
                            ConfigurationManager configurationManager) {
        super(view);
        this.configurationManager = configurationManager;
    }


    @Override
    public void onStart() {
        super.onStart();
        loadFcraDisclosureAgreement();
    }

    private String findCityName(int cityId) {
        for (SupportedCity city : configurationManager.getLastConfiguration().getSupportedCities()) {
            if (city.getCityId() == cityId) {
                return city.getCityName();
            }
        }
        throw new IllegalStateException("City not found");
    }

    private void loadFcraDisclosureAgreement() {
        addSubscription(
                Observable.just(App.getDataManager().getOrCreateDriverRegistrationData())
                        .map(DriverRegistrationData::getDriverRegistration)
                        .map(DriverRegistration::getCityId)
                        .map(this::findCityName)
                        .flatMap(this::loadDisclosureAgreement)
                        .subscribeOn(RxSchedulers.network())
                        .observeOn(RxSchedulers.main())
                        .subscribe(this::onDisclosureAgreementAvailable, new DefaultErrorAction())
        );
    }

    private Observable<String> loadDisclosureAgreement(String cityName) {
        return Observable.fromCallable(() -> {
            return AssetsHelper.loadAssetTextAsString(App.getInstance(), "fcra_disclosure/" + cityName.toLowerCase() + ".txt");
        });
    }

    private void onDisclosureAgreementAvailable(String disclosureAgreement) {
        performOnView(view -> {
            view.onDisclosureAgreementUpdated(disclosureAgreement);
        });
    }

}
