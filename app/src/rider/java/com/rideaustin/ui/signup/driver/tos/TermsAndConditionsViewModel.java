package com.rideaustin.ui.signup.driver.tos;

import android.support.annotation.NonNull;

import com.rideaustin.api.model.driver.DriverSignUpResult;
import com.rideaustin.models.TermsResponse;
import com.rideaustin.schedulers.RxSchedulers;
import com.rideaustin.ui.common.BaseViewModel;
import com.rideaustin.ui.common.DefaultErrorAction;
import com.rideaustin.ui.signup.driver.DriverSignUpInteractor;

import timber.log.Timber;

/**
 * Created by hatak on 07.12.16.
 */

public class TermsAndConditionsViewModel extends BaseViewModel<TermsAndConditionsView> {

    private final DriverSignUpInteractor signUpInteractor;

    public TermsAndConditionsViewModel(@NonNull TermsAndConditionsView view, DriverSignUpInteractor signUpInteractor) {
        super(view);
        this.signUpInteractor = signUpInteractor;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadTermsOfService();
    }

    public void loadTermsOfService() {
        performOnView(TermsAndConditionsView::onTermsLoading);
        addSubscription(signUpInteractor.obtainTermsAndConditions()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(this::doOnTermsLoaded, new DefaultErrorAction("Unexpected error while loading terms")));
    }

    private void doOnTermsLoaded(@NonNull TermsResponse response) {
        performOnView(view -> {
            if (response.isSuccessfull()) {
                view.onTermsUpdated(response.getTerms());
            } else {
                view.onTermsError(response.getError(), response.shouldRetry());
            }
        });
    }

    public void createNewDriver() {
        performOnView(view -> view.getCallback().showProgress());
        signUpInteractor.signUpNewDriver()
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(this::uploadDriverPhoto, throwable -> {
                    Timber.e(throwable, "Cannot sign up driver");
                    performOnView(view -> {
                        view.getCallback().hideProgress();
                        view.onShowCreateDriverErrorDialog(throwable.getMessage());
                    });
                });
    }

    public void uploadDriverPhoto(DriverSignUpResult result) {
        signUpInteractor.uploadDriverPhoto(result)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(driver -> uploadCarData(result), throwable -> {
                    Timber.e(throwable, "Cannot upload driver photo");
                    performOnView(view -> {
                        view.getCallback().hideProgress();
                        view.onShowDriverPhotoErrorDialog(result);
                    });
                });
    }

    public void uploadCarData(DriverSignUpResult result) {
        signUpInteractor.uploadCarData(result)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(this::uploadCarPhotoData, throwable -> {
                    Timber.e(throwable, "Cannot upload car data");
                    performOnView(view -> {
                        view.getCallback().hideProgress();
                        view.onShowCarDataErrorDialog(result);
                    });
                });
    }

    public void uploadCarPhotoData(DriverSignUpResult result) {
        signUpInteractor.uploadCarPhotoData(result)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(driverSignUpResult -> uploadOptionalTNCData(result), throwable -> {
                    Timber.e(throwable, "Cannot upload car photos");
                    performOnView(view -> {
                        view.getCallback().hideProgress();
                        view.onShowCarPhotoErrorDialog(result);
                    });
                });
    }

    public void uploadOptionalTNCData(final DriverSignUpResult result) {
        signUpInteractor.uploadTNCCardData(result)
                .flatMap(signUpInteractor::uploadTNCStickerData)
                .subscribeOn(RxSchedulers.network())
                .observeOn(RxSchedulers.main())
                .subscribe(results -> {
                    performOnView(view -> {
                        view.getCallback().hideProgress();
                        view.onCompleted();
                    });
                }, throwable -> {
                    Timber.e(throwable, "Cannot TNC data");
                    performOnView(view -> {
                        view.getCallback().hideProgress();
                        view.onShowTNCDataErrorDialog(result);
                    });
                });
    }

}
