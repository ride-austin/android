package com.rideaustin.ui.signup.driver;

import android.text.TextUtils;

import com.rideaustin.App;
import com.rideaustin.api.DataManager;
import com.rideaustin.api.config.DriverRegistration;
import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.config.SupportedCity;
import com.rideaustin.api.config.TncCard;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.api.model.driver.CurrentTerms;
import com.rideaustin.api.model.driver.Driver;
import com.rideaustin.api.model.driver.DriverSignUpResult;
import com.rideaustin.models.DriverRegistrationData;
import com.rideaustin.models.TermsResponse;
import com.rideaustin.utils.Constants;
import com.rideaustin.utils.TermsUtils;

import java.io.Serializable;

import rx.Observable;

/**
 * Created by hatak on 21.11.16.
 */

public class DriverSignUpInteractor implements Serializable {

    private long selectedCityId;
    private DriverRegistration driverRegistrationConfiguration;
    private GlobalConfig globalConfigInRegistration;
    private boolean isFrontTNCSkipped;
    private CurrentTerms currentTerms;

    private transient DataManager dataManager = App.getDataManager();

    public DriverSignUpInteractor() {
    }

    public DriverSignUpInteractor(final DataManager dataManager) {
        this.dataManager = dataManager;
    }

    private DataManager getDataManager() {
        if (dataManager == null) {
            dataManager = App.getDataManager();
        }
        return dataManager;
    }

    public void setFrontTNCSkipped(boolean frontTNCSkipped) {
        isFrontTNCSkipped = frontTNCSkipped;
    }

    public String getCityName() {
        for (SupportedCity city : getGlobalConfigInRegistration().getSupportedCities()) {
            if (city.getCityId() == getSelectedCityId()) {
                return city.getCityName();
            }
        }
        throw new IllegalStateException("City not found");
    }

    public GlobalConfig getGlobalConfigInRegistration() {
        return globalConfigInRegistration;
    }

    public void setGlobalConfigInRegistration(final GlobalConfig globalConfigInRegistration) {
        this.globalConfigInRegistration = globalConfigInRegistration;
    }

    public DriverRegistration getDriverRegistrationConfiguration() {
        return driverRegistrationConfiguration;
    }

    public void setDriverRegistrationConfiguration(final DriverRegistration driverRegistrationConfiguration) {
        this.driverRegistrationConfiguration = driverRegistrationConfiguration;
    }

    public void setSelectedCityId(final long selectedCityId) {
        this.selectedCityId = selectedCityId;
    }

    public long getSelectedCityId() {
        return selectedCityId;
    }

    public DriverRegistrationData getDriverRegistrationData() {
        final DriverRegistrationData registrationData = App.getDataManager().getOrCreateDriverRegistrationData();
        registrationData.setAcceptedTermId(currentTerms != null ? currentTerms.getId() : 1L);
        registrationData.getDriverRegistration().setCityId((int) selectedCityId);
        return registrationData;
    }

    public Observable<DriverSignUpResult> signUpNewDriver() {
        return getDataManager().createNewDriver(getDriverRegistrationData())
                .map(driver -> {
                    getDataManager().getCurrentUser().setAvatars(driver.getUser().getAvatars());
                    final DriverSignUpResult driverSignUpResult = new DriverSignUpResult();
                    driverSignUpResult.setDriver(driver);
                    return driverSignUpResult;
                });
    }

    public Observable<DriverSignUpResult> uploadCarData(DriverSignUpResult driverSignUpResult) {
        return getDataManager().uploadCarData(getDriverRegistrationData(), driverSignUpResult.getDriver().getUuid())
                .map(car -> {
                    driverSignUpResult.setCar(car);
                    return driverSignUpResult;
                });
    }

    public Observable<DriverSignUpResult> uploadCarPhotoData(DriverSignUpResult driverSignUpResult) {
        return getDataManager().uploadCarPhotoData(getDriverRegistrationData(), driverSignUpResult.getCar().getId())
                .map(car -> driverSignUpResult)
                .last();
    }

    public Observable<DriverSignUpResult> uploadTNCCardData(final DriverSignUpResult driverSignUpResult) {
        if (TextUtils.isEmpty(getDriverRegistrationData().getDriverTncCardImagePath())) {
            return Observable.just(driverSignUpResult);
        }

        return getDataManager().uploadTNCCard(
                getDriverRegistrationData(),
                driverSignUpResult.getDriver().getId(),
                getSelectedCityId())
                .map(driver -> driverSignUpResult);
    }

    public Observable<DriverSignUpResult> uploadTNCStickerData(final DriverSignUpResult driverSignUpResult) {
        if (TextUtils.isEmpty(getDriverRegistrationData().getDriverTncStickerImagePath())) {
            return Observable.just(driverSignUpResult);
        }

        return getDataManager().uploadTNCSticker(getDriverRegistrationData(),
                driverSignUpResult.getCar().getId(),
                driverSignUpResult.getDriver().getId(),
                getSelectedCityId())
                .map(driver -> driverSignUpResult);
    }

    public boolean shouldSkipTNCCardStep(final String subType) {
        final TncCard tncCard = driverRegistrationConfiguration.getTncCard();
        return !tncCard.getEnabled()
                || (tncCard.getEnabled() && subType.equals(Constants.TNCCardSide.BACK) && !tncCard.getBackPhotoEnabled())
                || (subType.equals(Constants.TNCCardSide.BACK) && isFrontTNCSkipped);
    }

    public boolean shouldSkipTNCStickerStep() {
        final Car carRegistration = getDriverRegistrationData()
                .getDriverRegistration()
                .getCars()
                .get(0);

        return !driverRegistrationConfiguration.getInspectionSticker().getEnabled() ||
                (driverRegistrationConfiguration.getInspectionSticker().getEnabled()
                        && Integer.parseInt(carRegistration.getYear()) >
                        driverRegistrationConfiguration.getInspectionSticker().getStickerRequiredYear());
    }

    public Observable<Driver> uploadDriverPhoto(DriverSignUpResult driverSignUpResult) {
        return getDataManager().postDriverPhoto(driverSignUpResult.getDriver().getId(), getDriverRegistrationData().getDriverPhotoImagePath());
    }

    public Observable<TermsResponse> obtainTermsAndConditions() {
        currentTerms = App.getConfigurationManager().getLastConfiguration().getCurrentTerms();
        String termsUrl = currentTerms != null && !TextUtils.isEmpty(currentTerms.getUrl())
                // Use recent terms url
                ? currentTerms.getUrl()
                // Use legacy url by default
                : driverRegistrationConfiguration.getDriverRegistrationTermsUrl();
        return TermsUtils.obtainTermsAndConditions(termsUrl);
    }
}
