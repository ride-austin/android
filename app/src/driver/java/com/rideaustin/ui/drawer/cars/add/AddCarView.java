package com.rideaustin.ui.drawer.cars.add;

import android.content.Context;

import com.rideaustin.api.config.GlobalConfig;
import com.rideaustin.api.model.driver.Car;
import com.rideaustin.base.BaseActivityCallback;
import com.rideaustin.base.BaseApiException;
import com.rideaustin.ui.common.BaseView;
import com.rideaustin.ui.common.TakePhotoFragment;

/**
 * Created by hatak on 1/30/17.
 */

public interface AddCarView extends BaseActivityCallback, BaseView, TakePhotoFragment.TakePhotoListenerContainer {

    void onCompleted(AddCarActivity.AddCarSequence sequence);

    void onConfigUpdated(final GlobalConfig globalConfig);

    AddCarViewModel getCarViewModel();

    Context getContext();

    BaseActivityCallback getCallback();

    void onCarCreateFailed(BaseApiException e);

    void onCarCreated(Car car);

    void onCarPhotosUploaded(Car car);

    void onCarPhotosUploadFailed(BaseApiException e);

    void onStickerUploaded(Car car);

    void onStickerUploadFailed(BaseApiException e);

    void onInsuranceUploadFailed(BaseApiException e);

    void onInsuranceUploaded(Car car);

    void onVehicleRequirementsFailed();

    void onCarUpdateFailed(BaseApiException e);

    void onCarUpdated(Car car);

    void carCancelled();

    void carCancelledFailed();
}
