package com.rideaustin.ui.signup.driver.tos;

import com.rideaustin.api.model.driver.DriverSignUpResult;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.ui.common.BaseView;

interface TermsAndConditionsView extends BaseView {
    BaseActivityCallback getCallback();

    void onCompleted();

    void onTermsLoading();

    void onTermsUpdated(String terms);

    void onTermsError(String error, boolean shouldRetry);

    void onShowTNCDataErrorDialog(final DriverSignUpResult result);

    void onShowCarPhotoErrorDialog(final DriverSignUpResult result);

    void onShowCarDataErrorDialog(final DriverSignUpResult result);

    void onShowDriverPhotoErrorDialog(final DriverSignUpResult result);

    void onShowCreateDriverErrorDialog(String message);
}